package voxels.map;

import java.util.*;

import com.jme3.math.*;

public class Coord3 {
	public final int x,y,z;
	
	public Coord3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Coord3(Vector3f vec) {
		this.x = (int)vec.x;
		this.y = (int)vec.z;
		this.z = (int)vec.y;
	}
	
	public Vector3f asVector() {
		return new Vector3f(x,z,y);
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

	public Coord3 times(Coord3 c) {
		return new Coord3(x * c.x, y * c.y, z * c.z);
	}

	public Coord3 times(double d) {
		return new Coord3((int)(x * d), (int)(y * d), (int)(z * d));
	}
	
	public static Coord3 multiply(Coord3 c1, Coord3 c2) {
		return c1.times(c2);
	}

	public static Coord3 multiply(Coord3 c, double d) {
		return new Coord3((int)(c.x * d), (int)(c.y * d), (int)(c.z * d));
	}
	
	public Coord3 divBy(Coord3 c) {
		return new Coord3(x / c.x, y / c.y, z / c.y);
	}

	public Coord3 divBy(double d) {
		return new Coord3((int)(x / d), (int)(y / d), (int)(z / d));
	}
	
	public static Coord3 divide(Coord3 c1, Coord3 c2) {
		return c1.divBy(c2);
	}

	public static Coord3 divide(Coord3 c, double d) {
		return new Coord3((int)(c.x / d), (int)(c.y / d), (int)(c.z / d));
	}
	
	public int dot(Coord3 c) {
		return x*c.x + y*c.y + z*c.z;
	}
	
	public static int dot(Coord3 c1, Coord3 c2) {
		return c1.dot(c2);
	}
	
	public Coord3 cross(Coord3 c) {
		return new Coord3(
				y*c.z - c.y*z,
				z*c.x - c.z*x,
				x*c.y - c.x*y
		);
	}
	
	public static Coord3 cross(Coord3 c1, Coord3 c2) {
		return c1.cross(c2);
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public static double length(Coord3 c) {
		return c.length();
	}
	
	public int volume() {
		return x * y * z;
	}
	
	public static int volume(Coord3 c) {
		return c.volume();
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %d, %d)", x, y, z);
	}
	
	public static Coord3 c3(int x, int y, int z) {
		return new Coord3(x, y, z);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Coord3)) return false;
		Coord3 other = (Coord3) obj;
		if (x != other.x) return false;
		if (y != other.y) return false;
		if (z != other.z) return false;
		return true;
	}
}
