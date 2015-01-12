package voxels.entity;

import com.jme3.math.*;
import com.jme3.renderer.*;

public class FakePlayer implements Player{
	private Camera camera;
	
	public FakePlayer(Camera camera) {
		this.camera = camera;
	}
	
	@Override
	public Vector3f getLocation() {
		return camera.getLocation();
	}
}
