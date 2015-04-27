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
	
	private final int order;
	private final int invOrder;
	
	private Direction(int order, int dx, int dy, int dz, int... cornerdirs) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		
		offset = new Vector3f(Math.max(dx, 0), Math.max(dy, 0), Math.max(dz, 0));
		
		this.order = order;
		this.invOrder = (3-order) % 3;
		
		this.c3 = new Coord3(dx, dy, dz);
		
		for(int i = 0; i < cornerOffsets.length; i++) {
			cornerOffsets[i] = new Vector3f((cornerdirs[i*3] + 1)/2, (cornerdirs[i*3 + 1] + 1)/2, (cornerdirs[i*3 + 2] + 1)/2);
		}
	}
	
	public float getRotX(float... xyz) {
		return xyz[order];
	}
	
	public float getRotY(float... xyz) {
		return xyz[(order+1) % 3];
	}
	
	public float getRotZ(float... xyz) {
		return xyz[(order+2) % 3];
	}
	
	public Vector3f rot(Vector3f in) {
		float[] xyz = in.toArray(new float[3]);
		return new Vector3f(getRotX(xyz), getRotY(xyz), getRotZ(xyz));
	}
	
	public Vector3f rotLocal(Vector3f in) {
		float[] xyz = in.toArray(new float[3]);
		return in.set(getRotX(xyz), getRotY(xyz), getRotZ(xyz));
	}
	
	public float getUnrotX(float... xyz) {
		return xyz[invOrder];
	}
	
	public float getUnrotY(float... xyz) {
		return xyz[(invOrder+1) % 3];
	}
	
	public float getUnrotZ(float... xyz) {
		return xyz[(invOrder+2) % 3];
	}
	
	public Vector3f unrot(Vector3f in) {
		float[] xyz = in.toArray(new float[3]);
		return new Vector3f(getUnrotX(xyz), getUnrotY(xyz), getUnrotZ(xyz));
	}
	
	public Vector3f unrotLocal(Vector3f in) {
		float[] xyz = in.toArray(new float[3]);
		return in.set(getUnrotX(xyz), getUnrotY(xyz), getUnrotZ(xyz));
	}
	
	public float getPrimary(float... xyz) {
		return xyz[order];
	}
	
	public float getPrimary(Vector3f in) {
		return in.toArray(new float[3])[order];
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
