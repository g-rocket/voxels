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
	
	public Coord3 plus(Coord3 c) {
		return new Coord3(x+c.x, y+c.y, z+c.z);
	}
	
	public static Coord3 add(Coord3 c1, Coord3 c2) {
		return c1.plus(c2);
	}
	
	public Coord3 minus(Coord3 c) {
		return new Coord3(x-c.x, y-c.y, z-c.z);
	}
	
	public static Coord3 subtract(Coord3 c1, Coord3 c2) {
		return c1.minus(c2);
	}
}
