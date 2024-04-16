package com.technicjelle.bluemapmcmapsync.serializable;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.map.MapView;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

import static com.technicjelle.bluemapmcmapsync.Config.MARKER_SET_ID;

@ConfigSerializable
public class Square {
	public static class SquareCreateException extends Exception {
		private SquareCreateException(String message) {
			super(message);
		}
	}

	private final Integer centerX;
	private final Integer centerZ;
	private final Integer radius;

	private transient int blockTopLeftX, blockTopLeftZ, blockBottomRightX, blockBottomRightZ;
	private transient int tileTopLeftX, tileTopLeftZ, tileBottomRightX, tileBottomRightZ;

	@SuppressWarnings("unused") // Used by configurate serialization
	private Square() {
		centerX = null;
		centerZ = null;
		radius = null;
	}

	public Square(MapView mapView, BlueMapMap map) throws SquareCreateException {
		this.centerX = mapView.getCenterX();
		this.centerZ = mapView.getCenterZ();

		MapView.Scale scale = mapView.getScale();
		this.radius = getRadiusFromScale(scale);
		if (radius == -1) {
			throw new SquareCreateException("Map's scale is not recognized.");
		}
		init(map);
	}

	public boolean isValid() {
		return centerX != null && centerZ != null && radius != null;
	}

	public void init(BlueMapMap map) {
		blockTopLeftX = centerX - radius;
		blockTopLeftZ = centerZ - radius;
		blockBottomRightX = centerX + radius;
		blockBottomRightZ = centerZ + radius;

		Vector2i pos1 = map.posToTile(blockTopLeftX, blockTopLeftZ);
		tileTopLeftX = pos1.getX();
		tileTopLeftZ = pos1.getY();
		Vector2i pos2 = map.posToTile(blockBottomRightX, blockBottomRightZ);
		tileBottomRightX = pos2.getX();
		tileBottomRightZ = pos2.getY();
	}

	public boolean containsTile(int tx, int tz) {
		return tx >= tileTopLeftX && tx <= tileBottomRightX && tz >= tileTopLeftZ && tz <= tileBottomRightZ;
	}

	@Override
	public String toString() {
		return "Square: " + centerX + ", " + centerZ + " r=" + radius;
	}

	public void addDebugMarkerToBlueMapMap(BlueMapMap map) {
		MarkerSet markerSet = map.getMarkerSets().computeIfAbsent(MARKER_SET_ID, id -> MarkerSet.builder()
				.label(MARKER_SET_ID)
				.toggleable(true)
				.defaultHidden(true)
				.build());

		Shape shape = Shape.createRect(blockTopLeftX, blockTopLeftZ, blockBottomRightX, blockBottomRightZ);
		ShapeMarker marker =ShapeMarker.builder()
				.label(this.toString())
				.shape(shape, 64)
				.depthTestEnabled(false)
				.lineColor(new Color(0, 0, 255, 1f))
				.fillColor(new Color(0, 0, 200, 0.3f))
				.build();

		markerSet.put(this.toString(), marker);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Square that = (Square) obj;
		return centerX.equals(that.centerX) && centerZ.equals(that.centerZ) && radius.equals(that.radius);
	}

	@Override
	public int hashCode() {
		return Objects.hash(centerX, centerZ, radius);
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
