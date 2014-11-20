package voxels.map;

import java.util.*;

import com.jme3.math.Vector3f;

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
	
	public static Iterable<Coord3> range(Coord3 start, Coord3 end) {
		return new RangeIterator(start, end);
	}
	
	private static class RangeIterator implements Iterator<Coord3>, Iterable<Coord3> {
		private Coord3 start;
		private Coord3 end;
		private int x, y, z;
		
		public RangeIterator(Coord3 start, Coord3 end) {
			this.start = start;
			this.end = end;
			x = start.x - 1;
			y = start.y;
			z = start.z;
		}

		@Override
		public boolean hasNext() {
			return x < end.x &&
				   y < end.y &&
				   z < end.z && (
					   x < end.x - 1 ||
					   y < end.y - 1 ||
					   z < end.z - 1
				   );
		}

		/**
		 * WARNING: may return junk if hasNext() is (or has ever been) false
		 */
		@Override
		public Coord3 next() {
			x++;
			if(x >= end.x) {
				x = start.x;
				y++;
			}
			if(y >= end.y){
				y = start.y;
				z++;
			}
			return new Coord3(x,y,z);
		}

		/**
		 * can only be used once per instance
		 */
		@Override
		public Iterator<Coord3> iterator() {
			return this;
		}
		
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
