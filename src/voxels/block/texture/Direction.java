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
	
	public final Vector3f primary;
	public final Vector3f secondary;
	public final int sign;
	
	private final Vector3f[] cornerOffsets = new Vector3f[4];
	
	public final Vector3f offset;
	
	private final int primaryComponentIndex;
	
	private Direction(int primaryComponentIndex, int dx, int dy, int dz, int... cornerDirs) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		
		offset = new Vector3f(Math.max(dx, 0), Math.max(dy, 0), Math.max(dz, 0));
		
		this.primaryComponentIndex = primaryComponentIndex;
		
		int[] primary = new int[3];
		primary[primaryComponentIndex] = 1;
		this.primary = new Vector3f(primary[0], primary[1], primary[2]);
		this.secondary = new Vector3f(1-primary[0], 1-primary[1], 1-primary[2]);
		sign = (int)getPrimaryComponent(offset);
		
		this.c3 = new Coord3(dx, dy, dz);
		
		for(int i = 0; i < cornerOffsets.length; i++) {
			cornerOffsets[i] = new Vector3f((cornerDirs[i*3] + 1)/2, (cornerDirs[i*3 + 1] + 1)/2, (cornerDirs[i*3 + 2] + 1)/2);
		}
	}
	
	public float getPrimaryComponent(Vector3f vector) {
		return vector.get(primaryComponentIndex);
	}
	
	public Vector2f getSecondaryComponents(Vector3f vector) {
		return new Vector2f(vector.get((primaryComponentIndex+1) % 3), vector.get((primaryComponentIndex+2) % 3));
	}
	
	public void setPrimaryComponent(Vector3f vector, float component) {
		vector.set(primaryComponentIndex, component);
	}
	
	public void getSecondaryComponents(Vector3f vector, Vector2f components) {
		vector.set((primaryComponentIndex+1) % 3, components.x);
		vector.set((primaryComponentIndex+2) % 3, components.y);
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
