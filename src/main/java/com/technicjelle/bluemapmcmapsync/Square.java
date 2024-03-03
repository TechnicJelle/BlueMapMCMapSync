package com.technicjelle.bluemapmcmapsync;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@ConfigSerializable
public class Square {
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

	public Square(SquareCreateInfo squareCreateInfo, BlueMapMap map) {
		this.centerX = squareCreateInfo.getCenter().getX();
		this.centerZ = squareCreateInfo.getCenter().getY();
		this.radius = squareCreateInfo.getRadius();
		init(map);
	}

	public Square(int centerX, int centerZ, int radius, BlueMapMap map) {
		this.centerX = centerX;
		this.centerZ = centerZ;
		this.radius = radius;
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
		String key = "MC Map Sync Debug Overlay";
		MarkerSet markerSet = map.getMarkerSets().computeIfAbsent(key, id -> MarkerSet.builder()
				.label(key)
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
}
