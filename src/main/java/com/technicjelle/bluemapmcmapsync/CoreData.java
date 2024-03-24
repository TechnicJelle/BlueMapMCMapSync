package com.technicjelle.bluemapmcmapsync;

import com.technicjelle.bluemapmcmapsync.serializable.MapExplorationData;
import com.technicjelle.bluemapmcmapsync.serializable.Square;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreData {
	public static final String CONF_EXT = ".conf";

	private final Logger logger;
	private final File dataFolder;

	private final Map<HashedBlueMapMap, MapExplorationData> explorationData;

	public CoreData(JavaPlugin plugin, BlueMapAPI blueMapAPI) {
		explorationData = new HashMap<>();

		logger = plugin.getLogger();
		logger.info("Loading existing configs");

		dataFolder = plugin.getDataFolder();

		File[] files = dataFolder.listFiles();
		if (files == null) return;

		for (File file : files) {
			loadConfig(blueMapAPI, file);
		}
	}

	private void loadConfig(BlueMapAPI blueMapAPI, File file) {
		if (!file.getName().endsWith(CONF_EXT)) return; //ignore non-config files

		String mapID = file.getName().substring(0, file.getName().length() - CONF_EXT.length()); //get mapID from filename

		BlueMapMap map = blueMapAPI.getMap(mapID).orElse(null);
		if (map == null) {
			logger.warning("No BlueMap Map was found with the ID: " + mapID);
			return;
		}
		logger.info("Loading config for map: " + map.getId());

		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.prettyPrinting(true)
				.path(file.toPath())
				.build();

		MapExplorationData mapExplorationData;
		try {
			CommentedConfigurationNode root = loader.load();
			if (root == null) throw new Exception("Failed to load config root for map: " + map.getId());
			if (root.virtual()) throw new Exception("Config's root property is was virtual for map: " + map.getId());
			mapExplorationData = root.get(MapExplorationData.class);
			if (mapExplorationData == null) throw new Exception("MapData was null for map:" + map.getId());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to load config for map: " + map.getId(), e);
			return;
		}

		explorationData.put(new HashedBlueMapMap(map), mapExplorationData);
		for (Square square : mapExplorationData.getSquares()) {
			if (!square.isValid()) {
				logger.severe("Invalid square found in config for map " + map.getId());
				continue;
			}
			square.init(map);
			if (mapExplorationData.isDebugMode()) {
				logger.info("Debug Mode is enabled, adding debug marker: " + square);
				square.addDebugMarkerToBlueMapMap(map);
			}
		}

		int squaresAmount = mapExplorationData.getSquares().size();

		logger.info("Loaded " + squaresAmount + " squares for map: " + map.getId() + " (Debug Mode: " + mapExplorationData.isDebugMode() + ")");
	}

	public void discoverMapView(Player player, MapView mapView) {
		World world = mapView.getWorld();
		if (world == null) {
			player.sendMessage(ChatColor.RED + "The world this map is associated with is not loaded.");
			return;
		}
		BlueMapAPI api = BlueMapAPI.getInstance().orElse(null);
		if (api == null) {
			player.sendMessage(ChatColor.RED + "BlueMap is not loaded. Try again later.");
			return;
		}
		BlueMapWorld blueMapWorld = api.getWorld(world).orElse(null);
		if (blueMapWorld == null) {
			player.sendMessage(ChatColor.RED + "Cannot find the BlueMapWorld of this Bukkit World.");
			return;
		}
		if (blueMapWorld.getMaps().isEmpty()) {
			player.sendMessage(ChatColor.RED + "No maps found for this world.");
			return;
		}

		// Discover every BlueMap map of this world
		for (BlueMapMap map : blueMapWorld.getMaps()) {
			HashedBlueMapMap hashedBMMap = new HashedBlueMapMap(map);
			if (!explorationData.containsKey(hashedBMMap)) continue; //ignore maps that do not have a config

			try {
				Square square = new Square(mapView, map);
				if (addSquareToMap(square, hashedBMMap)) {
					player.sendMessage("Discovered another piece of " + map.getName() + "!");
				} else {
					player.sendMessage("This part of " + map.getName() + " has already been discovered");
				}
			} catch (Square.SquareCreateException e) {
				player.sendMessage(ChatColor.RED + e.getMessage());
			}
		}
	}

	private boolean addSquareToMap(Square square, HashedBlueMapMap hashedBMMap) {
		MapExplorationData mapExplorationData = explorationData.get(hashedBMMap);
		boolean success = mapExplorationData.getSquares().add(square);
		String mapID = hashedBMMap.getMap().getId();
		try {
			mapExplorationData.save(dataFolder, mapID);
		} catch (ConfigurateException e) {
			logger.log(Level.SEVERE, "Failed to save config for map: " + mapID, e);
		}

		if (mapExplorationData.isDebugMode() && success) {
			logger.info("Debug Mode is enabled, adding debug marker: " + square.toString());
			square.addDebugMarkerToBlueMapMap(hashedBMMap.getMap());
		}

		return success;
	}

	public void setupTileFilters() {
		// Set tile filters and add debug markers
		for (Map.Entry<HashedBlueMapMap, MapExplorationData> entry : explorationData.entrySet()) {
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
}
