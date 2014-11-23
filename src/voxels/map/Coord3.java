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
	
	public static Coord3 multiply(Coord3 c1, Coord3 c2) {
		return c1.times(c2);
	}
	
	public Coord3 divBy(Coord3 c) {
		return new Coord3(x / c.x, y / c.y, z / c.y);
	}
	
	public static Coord3 divide(Coord3 c1, Coord3 c2) {
		return c1.divBy(c2);
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
	
	public static List<Coord3> range(Coord3 start, Coord3 size) {
		return new Range(start, size);
	}
	
	private static class Range extends AbstractList<Coord3> {
		private Coord3 start;
		private Coord3 size;
		
		public Range(Coord3 start, Coord3 size) {
			this.start = start;
			this.size = size;
		}

		@Override
		public Coord3 get(int i) {
			if(i < 0 || i >= size()) throw new IllegalArgumentException(i+" out of range for "+this);
			return new Coord3(i % size.x + start.x, (i/size.x) % size.y + start.y, i/(size.x*size.y) + start.z);
		}

		@Override
		public int size() {
			return size.x * size.y * size.z;
		}
		
		@Override
		public String toString() {
			return "{["+start+":"+start.plus(size)+"], "+size+"}";
		}
		
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
