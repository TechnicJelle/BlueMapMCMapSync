package com.technicjelle.bluemapmcmapsync.triggers;

import com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class PlayerMapHoldListener implements Listener {
	private final BlueMapMCMapSync plugin;

	public PlayerMapHoldListener(BlueMapMCMapSync plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		if (!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

		ItemStack itemStack = event.getItem();
		if (itemStack == null) return;

		Material material = itemStack.getType();
		if (!material.getKey().getKey().contains("map")) return;

		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
				() -> myPlayerCreateMapEvent(player), 1);
	}

	private void myPlayerCreateMapEvent(Player player) {
		//Find the newest map in the player's inventory:
		int highestID = -1;
		MapView highestMapView = null;

		for (ItemStack itemStack : player.getInventory()) {
			if (itemStack == null) continue;
			ItemMeta itemMeta = itemStack.getItemMeta();
			if (!(itemMeta instanceof MapMeta)) continue;
			MapMeta mapMeta = (MapMeta) itemMeta;
			MapView mapView = mapMeta.getMapView();
			if (mapView == null) continue;
			int id = mapView.getId();
			if (id > highestID) {
				highestID = id;
				highestMapView = mapView;
			}
		}
		if (highestID == -1) {
			player.sendMessage(ChatColor.RED + "No map found in your inventory..?");
			plugin.getLogger().warning("No map found in " + player.getName() + "'s inventory..?");
			return;
		}

		plugin.discoverMapView(player, highestMapView);
	}
}
