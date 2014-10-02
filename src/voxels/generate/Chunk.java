package voxels.generate;

import voxels.map.Coord3;

/*
 * 
 */
public class Chunk {
	public static final int XLENGTH = 16, YLENGTH = 16, ZLENGTH = 16;
	public final Coord3 position;
	public final ChunkBrain brain;
	private final TerrainMap map;
	public boolean meshBuilding;
	public boolean meshDirty;
}
