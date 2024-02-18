package com.technicjelle.bluemapmcmapsync;

import de.bluecolored.bluemap.api.BlueMapMap;

public class HashedBlueMapMap {
	private final BlueMapMap map;

	public HashedBlueMapMap(BlueMapMap map) {
		this.map = map;
	}

	public BlueMapMap getMap() {
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		HashedBlueMapMap that = (HashedBlueMapMap) obj;
		return map.getId().equals(that.map.getId());
	}

	@Override
	public int hashCode() {
		return map.getId().hashCode();
	}

	@Override
	public String toString() {
		return map.getId();
	}
}
