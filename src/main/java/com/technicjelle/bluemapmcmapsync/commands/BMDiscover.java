package com.technicjelle.bluemapmcmapsync.commands;

import com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BMDiscover implements CommandExecutor, TabCompleter {
	private final BlueMapMCMapSync plugin;

	public BMDiscover(BlueMapMCMapSync plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
			return true;
		}
		Player player = (Player) sender;
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		ItemMeta meta = heldItem.getItemMeta();
		if (!(meta instanceof MapMeta)) {
			sender.sendMessage(ChatColor.RED + "You must be holding a map in your main hand to sync it");
			return true;
		}
		MapMeta mapMeta = (MapMeta) meta;
		MapView mapView = mapMeta.getMapView();
		if (mapView == null) {
			sender.sendMessage(ChatColor.RED + "This map does not exist on the server.");
			return true;
		}

		plugin.discoverMapView(player, mapView);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		return Collections.emptyList();
	}
}
