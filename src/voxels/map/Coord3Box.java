package voxels.map;

import java.util.*;

import static voxels.map.Coord3.*;

public class Coord3Box implements Coord3Range {
	private final Coord3 start;
	private final Coord3 size;
	private final Coord3 end;
	
	public enum Alignment {
		START {
			@Override
			public Coord3 getStart(Coord3 anchor, Coord3 size) {
				return anchor;
			}
		},
		CENTER {
			@Override
			public Coord3 getStart(Coord3 anchor, Coord3 size) {
				return anchor.plus(size.divBy(2));
			}
		},
		END {
			@Override
			public Coord3 getStart(Coord3 anchor, Coord3 size) {
				return anchor.minus(size);
			}
		};

		public abstract Coord3 getStart(Coord3 anchor, Coord3 size);
		
	}
	
	public static Coord3Box startEnd(Coord3 start, Coord3 end) {
		return new Coord3Box(start, start.minus(end), end);
	}
	
	public static Coord3Box anchorSizeAlignment(Coord3 anchor, Coord3 size, Alignment alignment) {
		Coord3 start = alignment.getStart(anchor, size);
		return new Coord3Box(start, size, start.plus(size));
	}

	private Coord3Box(Coord3 start, Coord3 size, Coord3 end) {
		if(!start.plus(size).equals(end)) throw new IllegalArgumentException("Start + Size != End in private constructor of Coord3Box");
		this.start = start;
		this.size = size;
		this.end = end;
	}
	
	@Override
	public Iterator<Coord3> iterator() {
		return new Range(start, size).iterator();
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
	public boolean containsCoord(Coord3 c) {
		return c.x >= start.x && c.y >= start.y && c.z >= start.z && c.x < end.x && c.y < end.y && c.z < end.z;
	}

}
