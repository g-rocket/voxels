package voxels.block.texture;

import voxels.map.*;

import com.jme3.math.*;

public enum Direction {
	XNEG(0,  -1, 0, 0,  -1, 1, 1,  -1,-1, 1,  -1,-1,-1,  -1, 1,-1),
	XPOS(0,   1, 0, 0,   1,-1, 1,   1, 1, 1,   1, 1,-1,   1,-1,-1),
	YNEG(1,   0,-1, 0,  -1,-1, 1,   1,-1, 1,   1,-1,-1,  -1,-1,-1),
	YPOS(1,   0, 1, 0,   1, 1, 1,  -1, 1, 1,  -1, 1,-1,   1, 1,-1),
	ZNEG(2,   0, 0,-1,   1, 1,-1,  -1, 1,-1,  -1,-1,-1,   1,-1,-1),
	ZPOS(2,   0, 0, 1,  -1, 1, 1,   1, 1, 1,   1,-1, 1,  -1,-1, 1);
	
	public final int dx,dy,dz;
	
	public final Coord3 c3;
	
	private final Vector3f[] cornerOffsets = new Vector3f[4];
	
	public final Vector3f offset;
	
	private final int primaryComponentIndex;
	
	private Direction(int primaryComponentIndex, int dx, int dy, int dz, int... cornerdirs) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		
		offset = new Vector3f(Math.max(dx, 0), Math.max(dy, 0), Math.max(dz, 0));
		
		this.primaryComponentIndex = primaryComponentIndex;
		
		this.c3 = new Coord3(dx, dy, dz);
		
		for(int i = 0; i < cornerOffsets.length; i++) {
			cornerOffsets[i] = new Vector3f((cornerdirs[i*3] + 1)/2, (cornerdirs[i*3 + 1] + 1)/2, (cornerdirs[i*3 + 2] + 1)/2);
		}
	}
	
	public float getPrimaryComponent(Vector3f vector) {
		return getForIndex(vector, primaryComponentIndex);
	}
	
	public Vector2f getSecondaryComponents(Vector3f vector) {
		return new Vector2f(getForIndex(vector, (primaryComponentIndex+1) % 3), getForIndex(vector, (primaryComponentIndex+2) % 3));
	}
	
	public void setPrimaryComponent(Vector3f vector, float component) {
		setForIndex(vector, component, primaryComponentIndex);
	}
	
	public void getSecondaryComponents(Vector3f vector, Vector2f components) {
		setForIndex(vector, components.x, (primaryComponentIndex+1) % 3);
		setForIndex(vector, components.y, (primaryComponentIndex+2) % 3);
	}
	
	private float getForIndex(Vector3f vector, int index) {
		switch(index) {
		case 0: return vector.x;
		case 1: return vector.y;
		case 2: return vector.z;
		default: throw new IllegalArgumentException("Invalid index: "+index);
		}
	}
	
	private void setForIndex(Vector3f vector, float value, int index) {
		switch(index) {
		case 0: vector.x = value; return;
		case 1: vector.y = value; return;
		case 2: vector.z = value; return;
		default: throw new IllegalArgumentException("Invalid index: "+index);
		}
	}
	
	public Vector3f[] getCorners(Coord3 center) {
		return new Vector3f[]{
				center.asVector().add(cornerOffsets[0]),
				center.asVector().add(cornerOffsets[1]),
				center.asVector().add(cornerOffsets[2]),
				center.asVector().add(cornerOffsets[3]),
		};
	}
}
