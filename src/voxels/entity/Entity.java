package voxels.entity;

import voxels.map.*;

import com.jme3.math.*;
import com.jme3.scene.*;

public interface Entity {
	public Vector3f getLocation();
	public default Coord3 getBlockLocation() {
		return new Coord3(getLocation());
	}
}
