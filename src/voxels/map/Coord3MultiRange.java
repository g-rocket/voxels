package voxels.map;

import java.util.*;

public class Coord3MultiRange implements Coord3Range {
	private final Coord3Range[] ranges;
	
	public Coord3MultiRange(Coord3Range... ranges) {
		this.ranges = ranges;
	}

	@Override
	public Iterator<Coord3> iterator() {
		return new Iterator<Coord3>() {
			private int currentRangeId = 0;
			private Iterator<Coord3> currentRange = ranges[0].iterator();
			
			@Override
			public Coord3 next() {
				if(!currentRange.hasNext()) {
					currentRange = ranges[currentRangeId++].iterator();
				}
				return currentRange.next();
			}
			
			@Override
			public boolean hasNext() {
				return currentRangeId < ranges.length || currentRange.hasNext();
			}
		};
	}

	@Override
	public boolean containsCoord(Coord3 c) {
		for(Coord3Range range: ranges) {
			if(range.containsCoord(c)) return true;
		}
		return false;
	}

}
