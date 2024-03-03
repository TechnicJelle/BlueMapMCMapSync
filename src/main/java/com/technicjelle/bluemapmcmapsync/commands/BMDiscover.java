package com.technicjelle.bluemapmcmapsync.commands;

import com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync;
import com.technicjelle.bluemapmcmapsync.Square;
import com.technicjelle.bluemapmcmapsync.SquareCreateInfo;
import de.bluecolored.bluemap.api.BlueMapMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.technicjelle.bluemapmcmapsync.SquareCreateInfo.SquareCreateException;
import static com.technicjelle.bluemapmcmapsync.BlueMapMCMapSync.MapNotLoadedException;

public class BMDiscover implements CommandExecutor, TabCompleter {
	private final BlueMapMCMapSync plugin;

	public BMDiscover(BlueMapMCMapSync plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		SquareCreateInfo squareCreateInfo;
		try {
			squareCreateInfo = SquareCreateInfo.fromCommand(sender);
		} catch (SquareCreateException e) {
			sender.sendMessage(ChatColor.RED + e.getMessage());
			return true;
		}

		// Discover every BlueMap map of this world
		for (BlueMapMap map : squareCreateInfo.getBlueMapWorld().getMaps()) {
			Square square = new Square(squareCreateInfo, map);
			try {
				if (plugin.addSquareToMap(square, map)) {
					sender.sendMessage("Discovered another piece of " + map.getName() + "!");
				} else {
					sender.sendMessage("This part of " + map.getName() + " has already been discovered");
				}
			} catch (MapNotLoadedException ignored) {
				// This map is not being tracked by this plugin
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		return Collections.emptyList();
	}
}
