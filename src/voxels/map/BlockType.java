package voxels.map;

public enum BlockType {
	UNKNOWN(255),
	AIR(0),
	DIRT(1),
	ROCK(2),
	GRASS(3);
	
	public final byte dataValue;
	
	private static final BlockType[] blocks = new BlockType[256];
	
	static {
		for(BlockType type: BlockType.values()) blocks[type.dataValue & 0xff] = type;
	}
	
	private BlockType(int dataValue) {
		this.dataValue = (byte)dataValue;
	}
	
	public static BlockType getBlock(byte data) {
		return blocks[data & 0xff];
	}
	
	public static BlockType getBlock(int data) {
		if(data < 0) data &= 0xff;
		return blocks[data];
	}
}
