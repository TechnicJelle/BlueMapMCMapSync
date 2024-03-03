package com.technicjelle.bluemapmcmapsync.commands;

import com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync;
import com.technicjelle.bluemapmcmapsync.Square;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import org.bukkit.ChatColor;
import org.bukkit.World;
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
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		ItemMeta meta = heldItem.getItemMeta();
		if (!(meta instanceof MapMeta)) {
			player.sendMessage(ChatColor.RED + "You must be holding a map in your main hand to sync it");
			return true;
		}
		MapMeta mapMeta = (MapMeta) meta;
		MapView mapView = mapMeta.getMapView();
		if (mapView == null) {
			player.sendMessage(ChatColor.RED + "This map does not exist on the server.");
			return true;
		}
		World world = mapView.getWorld();
		if (world == null) {
			player.sendMessage(ChatColor.RED + "The world this map is associated with is not loaded.");
			return true;
		}
		MapView.Scale scale = mapView.getScale();
		int radius = getRadiusFromScale(scale);
		if (radius == -1) {
			player.sendMessage(ChatColor.RED + "Map's scale is not recognized.");
			return true;
		}
		BlueMapAPI api = BlueMapAPI.getInstance().orElse(null);
		if (api == null) {
			player.sendMessage(ChatColor.RED + "BlueMap is not loaded. Try again later.");
			return true;
		}
		BlueMapWorld blueMapWorld = api.getWorld(world).orElse(null);
		if (blueMapWorld == null) {
			player.sendMessage(ChatColor.RED + "Cannot find the BlueMapWorld of this Bukkit World.");
			return true;
		}
		if (blueMapWorld.getMaps().isEmpty()) {
			player.sendMessage(ChatColor.RED + "No maps found for this world.");
			return true;
		}

		// Discover every BlueMap map of this world
		for (BlueMapMap map : blueMapWorld.getMaps()) {
			Square square = new Square(mapView.getCenterX(), mapView.getCenterZ(), radius, map);
			if (plugin.addSquareToMap(square, map)) {
				player.sendMessage("Discovered another piece of " + map.getName() + "!");
			} else {
				player.sendMessage("This part of " + map.getName() + " has already been discovered");
			}
		}
		return true;
	}

	/**
	 * @param scale the scale of the map
	 * @return the radius of the map. -1 if the scale is not recognized
	 */
	private static int getRadiusFromScale(MapView.Scale scale) {
		switch (scale) {
			case CLOSEST:
				return 64;
			case CLOSE:
				return 128;
			case NORMAL:
				return 256;
			case FAR:
				return 512;
			case FARTHEST:
				return 1024;
			default:
				return -1;
		}
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		return Collections.emptyList();
	}
}
