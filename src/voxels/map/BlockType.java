package voxels.map;

public enum BlockType {
	UNKNOWN(255, true),
	AIR    (  0, false),
	DIRT   (  1, true),
	ROCK   (  2, true),
	GRASS  (  3, true);
	
	public final byte dataValue;
	public final boolean isSolid;
	public final boolean hasTexture;
	
	private static final BlockType[] blocks = new BlockType[256];
	
	static {
		for(BlockType type: BlockType.values()) blocks[type.dataValue & 0xff] = type;
	}
	
	private BlockType(int dataValue, boolean isSolid) {
		this.dataValue = (byte)dataValue;
		this.isSolid = isSolid;
		this.hasTexture = dataValue == 0; // is it air?
	}
	
	public static BlockType getBlock(byte data) {
		return blocks[data & 0xff];
	}
	
	public static BlockType getBlock(int data) {
		data &= 0xff;
		return blocks[data];
	}
}
