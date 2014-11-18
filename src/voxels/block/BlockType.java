package voxels.block;

import voxels.block.texture.*;
import voxels.map.*;

import com.jme3.math.*;

import static voxels.block.texture.Direction.*;
import static voxels.block.texture.TextureOrientation.*;

public enum BlockType {
	UNKNOWN(255,  true,  true,  true,  XPOS,3,0,U,  XNEG,3,0,U,  YPOS,3,0,U,  YNEG,3,0,U,  ZPOS,3,0,U, ZNEG,3,0,U),
	AIR    (  0, false, false, false),
	DIRT   (  1,  true,  true,  true,  XPOS,0,1,U,  XNEG,0,1,U,  YPOS,0,1,U,  YNEG,0,1,U,  ZPOS,0,1,U, ZNEG,0,1,U),
	ROCK   (  2,  true,  true,  true,  XPOS,1,1,U,  XNEG,1,1,U,  YPOS,1,1,U,  YNEG,1,1,U,  ZPOS,1,1,U, ZNEG,1,1,U),
	GRASS  (  3,  true,  true,  true,  XPOS,0,3,U,  XNEG,0,3,U,  YPOS,0,3,U,  YNEG,0,3,U,  ZPOS,0,0,U, ZNEG,0,1,U);
	
	public final byte dataValue;
	public final boolean isSolid;
	public final boolean isOpaque;
	public final boolean hasTexture;
	
	private final Vector2f[][] textures;
	
	private static final BlockType[] blocks = new BlockType[256];
	
	static {
		for(BlockType type: BlockType.values()) blocks[type.dataValue & 0xff] = type;
	}
	
	private BlockType(int dataValue, boolean isSolid, boolean isOpaque, boolean hasTexture, Object... textureData) {
		this.dataValue = (byte)dataValue;
		this.isSolid = isSolid;
		this.isOpaque = isOpaque;
		this.hasTexture = hasTexture;
		
		if(!this.hasTexture) {
			textures = null;
		} else {
			textures = new Vector2f[textureData.length/4][4];
			for(int i = 0; i < textureData.length/4; i++) {
				Direction side = (Direction)textureData[i*4];
				Vector2f offsetStart = new Vector2f((Integer)textureData[i*4 + 1], (Integer)textureData[i*4 + 2]);
				TextureOrientation orientation = (TextureOrientation)textureData[i*4 + 3];
				for(int j = 0; j < textures[i].length; j++) {
					textures[side.ordinal()][j] = offsetStart.add(orientation.corners[j]).divide(4);
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
		//System.out.println(this + ": " + textures);
		return textures[direction.ordinal()];
	}
}
