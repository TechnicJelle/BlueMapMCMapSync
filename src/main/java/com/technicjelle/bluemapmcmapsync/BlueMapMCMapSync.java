package com.technicjelle.bluemapmcmapsync;

import com.technicjelle.UpdateChecker;
import com.technicjelle.bluemapmcmapsync.serializable.MapExplorationData;
import com.technicjelle.bluemapmcmapsync.triggers.BMDiscover;
import com.technicjelle.bluemapmcmapsync.triggers.PlayerMapHoldListener;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;

import java.util.function.Consumer;
import java.util.logging.Level;

public final class BlueMapMCMapSync extends JavaPlugin {
	private UpdateChecker updateChecker;

	@SuppressWarnings("FieldCanBeLocal")
	private Config config;
	private PlayerMapHoldListener playerMapHoldListener;

	private CoreData coreData;

	@Override
	public void onEnable() {
		new Metrics(this, 21034);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapMCMapSync", getDescription().getVersion());
		updateChecker.checkAsync();

		BlueMapAPI.onEnable(onEnableListener);
		BlueMapAPI.onDisable(onDisableListener);

		// Register the command
		PluginCommand bmDiscover = Bukkit.getPluginCommand("bmdiscover");
		BMDiscover executor = new BMDiscover(this);
		if (bmDiscover != null) {
			bmDiscover.setExecutor(executor);
		} else {
			getLogger().severe("bmdiscover is null. This is not good. Please report this.");
		}
	}

	final Consumer<BlueMapAPI> onEnableListener = api -> {
		updateChecker.logUpdateMessage(getLogger());

		// First time? Create configs
		if (getDataFolder().mkdirs()) {
			getLogger().info("Created plugin config directory");

			for (BlueMapMap map : api.getMaps()) {
				// Create default empty config for map
				MapExplorationData mapExplorationData = new MapExplorationData();
				String mapID = map.getId();
				try {
					mapExplorationData.save(getDataFolder(), mapID);
				} catch (ConfigurateException e) {
					getLogger().log(Level.SEVERE, "Failed to save config for map: " + mapID, e);
				}
				getLogger().info("Created default config for map: " + map.getId());
			}
		}

		coreData = new CoreData(this, api);
		coreData.setupTileFilters();

		config = new Config(this);

		if (config.isAutomaticMapDiscovery()) {
			playerMapHoldListener = new PlayerMapHoldListener(this);
			getServer().getPluginManager().registerEvents(playerMapHoldListener, this);
		}
	};

	final Consumer<BlueMapAPI> onDisableListener = api -> {
		if (playerMapHoldListener != null) {
			HandlerList.unregisterAll(playerMapHoldListener);
			playerMapHoldListener = null;
		}
	};

	public void discoverMapView(Player player, MapView mapView) {
		coreData.discoverMapView(player, mapView);
		coreData.setupTileFilters();
	}
}
