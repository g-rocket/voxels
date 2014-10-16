package voxels.map;

public enum BlockType {
	UNKNOWN(255),
	AIR(0),
	DIRT(1),
	ROCK(2),
	GRASS(3);
	
	public final byte dataValue;
	
	private BlockType(int dataValue) {
		
	}
}
