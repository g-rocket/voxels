package voxels.map;

import com.jme3.math.Vector3f;

public class Coord3 {
	public final int x,y,z;
	
	public Coord3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3f asVector() {
		return new Vector3f(x,y,z);
	}
}
