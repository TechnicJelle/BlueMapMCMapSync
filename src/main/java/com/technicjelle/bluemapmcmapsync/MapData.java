package com.technicjelle.bluemapmcmapsync;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync.CONF_EXT;

@ConfigSerializable
public class MapData {
	private final boolean debugMode;
	private final Set<Square> squares;

	public MapData() {
		this.debugMode = false;
		this.squares = new HashSet<>();
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public Set<Square> getSquares() {
		return squares;
	}

	public void save(JavaPlugin plugin, String mapID) {
		Path mapConfigPath = plugin.getDataFolder().toPath().resolve(mapID + CONF_EXT);
		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.prettyPrinting(true)
				.path(mapConfigPath)
				.build();

		try {
			CommentedConfigurationNode root = loader.createNode();
			root.set(this);
			loader.save(root);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save config for map: " + mapID, e);
		}
	}
}
