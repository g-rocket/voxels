package voxels.entity;

import com.jme3.math.*;
import com.jme3.renderer.*;

public class FakePlayer extends Player {
	private Camera camera;
	
	public FakePlayer(Camera camera) {
		super(0,0,0);
		this.camera = camera;
	}
	
	@Override
	public Vector3f getLocation() {
		return camera.getLocation();
	}
}
