package com.technicjelle.bluemapmcmapsync;

import com.technicjelle.MCUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

public class Config {
	public static final String MARKER_SET_ID = "MC Map Sync Debug Overlay";

	private final BlueMapMCMapSync plugin;

	private final boolean automaticMapDiscovery;

	public Config(BlueMapMCMapSync plugin) {
		this.plugin = plugin;

		try {
			MCUtils.copyPluginResourceToConfigDir(plugin, "config.yml", "config.yml", false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		plugin.reloadConfig();

		automaticMapDiscovery = configFile().getBoolean("AutomaticMapDiscovery", false);
	}

	private FileConfiguration configFile() {
		return plugin.getConfig();
	}

	public boolean isAutomaticMapDiscovery() {
		return automaticMapDiscovery;
	}
}
