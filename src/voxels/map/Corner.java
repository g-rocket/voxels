package voxels.map;

import com.jme3.math.*;

public enum Corner {
	TL( 1, 1),
	TR(-1, 1),
	BR(-1,-1),
	BL(-1, 1);
	
	public final Vector2f position;
	
	private Corner(int x, int y) {
		this.position = new Vector2f(x, y);
	}
}