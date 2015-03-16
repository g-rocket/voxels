package voxels.entity;

import com.jme3.bullet.*;
import com.jme3.bullet.control.*;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;

public class MainPlayer extends BetterCharacterControl implements Player, ActionListener {
	private final Camera camera;

	private float sideSpeed = 1;
	private float frontSpeed = 2;
	private float backSpeed = .5f;

	public MainPlayer(Camera camera) {
		super(.5f, 1.8f, 1f);
		setSpatial(new Node("MainPlayer"));
		setGravity(new Vector3f(0,1,0));
		setPhysicsLocation(new Vector3f(0,100,0));
		camera.setLocation(getLocation());
		this.camera = camera;
	}

	@Override
	public Vector3f getLocation() {
		return super.getSpatialTranslation();
	}

	@Override
	public void physicsTick(PhysicsSpace space, float tpf) {
		super.physicsTick(space, tpf);
		camera.setLocation(super.getSpatialTranslation());
		camera.setRotation(super.getSpatialRotation());
	};
	
	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		Vector3f walkDirection = new Vector3f(0,0,0);
		switch(name) {
		case "Left": walkDirection.addLocal(camera.getLeft().mult(sideSpeed));
		case "Right": walkDirection.addLocal(camera.getLeft().mult(-sideSpeed));
		case "Up": walkDirection.addLocal(camera.getDirection().mult(frontSpeed));
		case "Down": walkDirection.addLocal(camera.getDirection().mult(-backSpeed));
		case "Jump": super.jump();
		}
		setWalkDirection(walkDirection);
	}
}
