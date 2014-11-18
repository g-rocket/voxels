package voxels.block.texture;

import com.jme3.math.*;

public enum Corner {
	TL(0,1),
	TR(1,1),
	BR(1,0),
	BL(0,0);
	
	public final Vector2f position;
	
	private Corner(int x, int y) {
		this.position = new Vector2f(x, y);
	}
}