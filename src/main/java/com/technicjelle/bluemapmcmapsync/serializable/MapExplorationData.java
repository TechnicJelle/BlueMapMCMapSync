package com.technicjelle.bluemapmcmapsync.serializable;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static com.technicjelle.bluemapmcmapsync.CoreData.CONF_EXT;

@ConfigSerializable
public class MapExplorationData {
	private final boolean debugMode;
	private final Set<Square> squares;

	public MapExplorationData() {
		this.debugMode = false;
		this.squares = new HashSet<>();
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public Set<Square> getSquares() {
		return squares;
	}

	public void save(File dataFolder, String mapID) throws ConfigurateException {
		Path mapConfigPath = dataFolder.toPath().resolve(mapID + CONF_EXT);
		HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
				.prettyPrinting(true)
				.path(mapConfigPath)
				.build();

		CommentedConfigurationNode root = loader.createNode();
		root.set(this);
		loader.save(root);
	}
}
