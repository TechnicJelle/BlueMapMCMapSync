package com.technicjelle.bluemapmcmapsync;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

public class SquareCreateInfo {
	public static class SquareCreateException extends Exception{
		private SquareCreateException(String message) {
			super(message);
		}
	}

	final BlueMapWorld blueMapWorld;
	final Vector2i center;
	final int radius;

	public static SquareCreateInfo fromCommand(CommandSender sender) throws SquareCreateException {
		if (!(sender instanceof Player)) {
			throw new SquareCreateException("You must be a player to use this command.");
		}
		Player player = (Player) sender;
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		ItemMeta meta = heldItem.getItemMeta();
		if (!(meta instanceof MapMeta)) {
			throw new SquareCreateException("You must be holding a map in your main hand to sync it");
		}
		MapMeta mapMeta = (MapMeta) meta;
		MapView mapView = mapMeta.getMapView();
		if (mapView == null) {
			throw new SquareCreateException("This map does not exist on the server.");
		}

		return new SquareCreateInfo(mapView);
	}

	public SquareCreateInfo(MapView mapView) throws SquareCreateException {
		World world = mapView.getWorld();
		if (world == null) {
			throw new SquareCreateException("The world this map is associated with is not loaded.");
		}
		MapView.Scale scale = mapView.getScale();
		radius = getRadiusFromScale(scale);
		if (radius == -1) {
			throw new SquareCreateException("Map's scale is not recognized.");
		}
		BlueMapAPI api = BlueMapAPI.getInstance().orElse(null);
		if (api == null) {
			throw new SquareCreateException("BlueMap is not loaded. Try again later.");
		}
		blueMapWorld = api.getWorld(world).orElse(null);
		if (blueMapWorld == null) {
			throw new SquareCreateException("Cannot find the BlueMapWorld of this Bukkit World.");
		}
		if (blueMapWorld.getMaps().isEmpty()) {
			throw new SquareCreateException("No maps found for this world.");
		}

		center = Vector2i.from(mapView.getCenterX(), mapView.getCenterZ());
	}

	public BlueMapWorld getBlueMapWorld() {
		return blueMapWorld;
	}

	public Vector2i getCenter() {
		return center;
	}

	public int getRadius() {
		return radius;
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
}
