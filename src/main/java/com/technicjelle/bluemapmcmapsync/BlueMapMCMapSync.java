package com.technicjelle.bluemapmcmapsync;

import com.technicjelle.UpdateChecker;
import com.technicjelle.bluemapmcmapsync.commands.BMDiscover;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class BlueMapMCMapSync extends JavaPlugin {
	public static final String CONF_EXT = ".conf";
	private UpdateChecker updateChecker;

	private Map<HashedBlueMapMap, MapData> squaresMap;

	public boolean addSquareToMap(Square square, BlueMapMap map) {
		HashedBlueMapMap hashedBMMap = new HashedBlueMapMap(map);
		MapData mapData = squaresMap.get(hashedBMMap);
		if (mapData.isDebugMode()) {
			getLogger().info("Debug Mode is enabled, adding debug marker: " + square.toString());
			square.addDebugMarkerToBlueMapMap(map);
		}
		boolean success = mapData.getSquares().add(square);
		mapData.save(this, map.getId());

		setupTileFilters();
		return success;
	}

	@Override
	public void onEnable() {
		new Metrics(this, 21034);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapMCMapSync", getDescription().getVersion());
		updateChecker.checkAsync();

		BlueMapAPI.onEnable(onEnableListener);

		// Register the command
		PluginCommand bmDiscover = Bukkit.getPluginCommand("bmdiscover");
		BMDiscover executor = new BMDiscover(this);
		if (bmDiscover != null) {
			bmDiscover.setExecutor(executor);
			bmDiscover.setTabCompleter(executor);
		} else {
			getLogger().warning("bmdiscover is null. This is not good");
		}
	}

	final Consumer<BlueMapAPI> onEnableListener = api -> {
		api.getRenderManager().stop(); //do not render anything yet, until the squares are loaded
		updateChecker.logUpdateMessage(getLogger());

		// First time? Create configs
		if (getDataFolder().mkdirs()) {
			getLogger().info("Created plugin config directory");

			for (BlueMapMap map : api.getMaps()) {
				// Create default empty config for map
				MapData mapData = new MapData();
				mapData.save(this, map.getId());
				getLogger().info("Created default config for map: " + map.getId());
			}
		}

		loadConfigs(api);

		api.getRenderManager().start(); //the squares have been loaded, we may start rendering now
		setupTileFilters();
	};

	private void setupTileFilters() {
		// Set tile filters and add debug markers
		for (Map.Entry<HashedBlueMapMap, MapData> entry : squaresMap.entrySet()) {
			BlueMapMap map = entry.getKey().getMap();
			Set<Square> squares = entry.getValue().getSquares();

			map.setTileFilter(tilePos -> {
				for (Square square : squares) {
					if (square.containsTile(tilePos.getX(), tilePos.getY())) {
						return true;
					}
				}
				return false;
			});
		}
	}

	private void loadConfigs(BlueMapAPI api) {
		getLogger().info("Loading existing configs");
		squaresMap = new HashMap<>();

		File configPath = getDataFolder();
		File[] files = configPath.listFiles();
		if (files == null) return;

		for (File file : files) {
			loadConfig(api, file);
		}
	}

	private void loadConfig(BlueMapAPI api, File file) {
		if (!file.getName().endsWith(CONF_EXT)) return; //ignore non-config files

		String mapID = file.getName().substring(0, file.getName().length() - CONF_EXT.length()); //get mapID from filename

		BlueMapMap map = api.getMap(mapID).orElse(null);
		if (map == null) {
			getLogger().warning("No BlueMap Map was found with the ID: " + mapID);
			return;
		}
		getLogger().info("Loading config for map: " + map.getId());

		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.prettyPrinting(true)
				.path(file.toPath())
				.build();

		MapData mapData;
		try {
			CommentedConfigurationNode root = loader.load();
			if (root == null) throw new Exception("Failed to load config root for map: " + map.getId());
			if (root.virtual()) throw new Exception("Config's root property is was virtual for map: " + map.getId());
			mapData = root.get(MapData.class);
			if (mapData == null) throw new Exception("MapData was null for map:" + map.getId());
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Failed to load config for map: " + map.getId(), e);
			return;
		}

		squaresMap.put(new HashedBlueMapMap(map), mapData);
		for (Square square : mapData.getSquares()) {
			if (!square.isValid()) {
				getLogger().severe("Invalid square found in config for map " + map.getId());
				continue;
			}
			square.init(map);
			if (mapData.isDebugMode()) {
				getLogger().info("Debug Mode is enabled, adding debug marker: " + square);
				square.addDebugMarkerToBlueMapMap(map);
			}
		}

		int squaresAmount = mapData.getSquares().size();

		getLogger().info("Loaded " + squaresAmount + " squares for map: " + map.getId() + " (Debug Mode: " + mapData.isDebugMode() + ")");
	}
}
