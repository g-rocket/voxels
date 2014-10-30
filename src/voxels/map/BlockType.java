package voxels.map;

import com.jme3.math.*;

public enum BlockType {
	UNKNOWN(255,  true),
	AIR    (  0, false),
	DIRT   (  1,  true),
	ROCK   (  2,  true),
	GRASS  (  3,  true);
	
	public final byte dataValue;
	public final boolean isSolid;
	public final boolean hasTexture;
	
	private final Vector2f[][] textures;
	
	private static final BlockType[] blocks = new BlockType[256];
	
	static {
		for(BlockType type: BlockType.values()) blocks[type.dataValue & 0xff] = type;
	}
	
	private BlockType(int dataValue, boolean isSolid, Object... textureData) {
		this.dataValue = (byte)dataValue;
		this.isSolid = isSolid;
		this.hasTexture = dataValue == 0; // is it air?
		
		if(!this.hasTexture) {
			textures = null;
		} else {
			textures = new Vector2f[textureData.length/3][4];
			for(int i = 0; i < textureData.length/3; i++) {
				Vector2f offsetStart = new Vector2f((Integer)textureData[i*3], (Integer)textureData[i*3 + 1]);
				TextureOrientation orientation = (TextureOrientation)textureData[i*3 + 2];
				for(int j = 0; j < textures[i].length; j++) {
					textures[i][j] = offsetStart.add(orientation.corners[j]).divide(4);
				}
			}
		}
	}
	
	public static BlockType getBlock(byte data) {
		return blocks[data & 0xff];
	}
	
	public static BlockType getBlock(int data) {
		data &= 0xff;
		return blocks[data];
	}

	public Vector2f[] getTexture(Direction direction) {
		return textures[direction.ordinal()];
	}
}
