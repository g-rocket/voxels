package voxels.map;

import com.jme3.math.Vector3f;

public enum Direction {
	XNEG(-1, 0, 0,  -1, 1, 1,  -1,-1, 1,  -1,-1,-1,  -1, 1,-1),
	XPOS( 1, 0, 0,   1, 1, 1,   1, 1,-1,   1,-1,-1,   1,-1, 1),
	YNEG( 0,-1, 0,   1,-1, 1,   1,-1,-1,  -1,-1,-1,  -1,-1, 1),
	YPOS( 0, 1, 0,   1, 1, 1,  -1, 1, 1,  -1, 1,-1,   1, 1,-1),
	ZNEG( 0, 0,-1,   1, 1,-1,  -1, 1,-1,  -1,-1,-1,   1,-1,-1),
	ZPOS( 0, 0, 1,   1, 1, 1,   1,-1, 1,  -1,-1, 1,  -1, 1, 1);
	
	public final int dx,dy,dz;
	
	private final Vector3f[] cornerOffsets = new Vector3f[4];
	
	private Direction(int dx, int dy, int dz, int... cornerdirs) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		
		for(int i = 0; i < cornerOffsets.length; i++) {
			cornerOffsets[i] = new Vector3f(cornerdirs[i*3]/2f, cornerdirs[i*3 + 1]/2f, cornerdirs[i*3 + 2]/2f);
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
