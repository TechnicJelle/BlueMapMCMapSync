package com.technicjelle.bluemapmcmapsync;

import com.technicjelle.bluemapmcmapsync.commands.BMDiscover;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.map.MapView;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import org.bukkit.entity.Player;

public class PlayerMapHoldListener implements Listener {

	private final BlueMapMCMapSync plugin;

	public PlayerMapHoldListener(BlueMapMCMapSync plugin) {
		this.plugin = plugin; 
		plugin.getLogger().info("PlayerMapHoldListener registered");
	}

	@EventHandler
	public void onMapInitialize(MapInitializeEvent event) {
		MapView mapView = event.getMap();
		BMDiscover executor = plugin.getExecutor();
		executor.discoverMap(mapView);
	}
}
