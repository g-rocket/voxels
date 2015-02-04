package voxels.map;

public interface Coord3Range extends Iterable<Coord3> {
	public boolean containsCoord(Coord3 c);
}
