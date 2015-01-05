package voxels.entity;

import java.io.*;

import voxels.map.*;

import com.jme3.bullet.*;
import com.jme3.bullet.control.*;
import com.jme3.export.*;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.*;

public class MainPlayer extends BetterCharacterControl implements Player, ActionListener {
	private final Camera camera;

	private float sideSpeed = 1;
	private float frontSpeed = 2;
	private float backSpeed = .5f;

	public MainPlayer(Camera camera) {
		super(.5f, 1.8f, 1f);
		setGravity(new Vector3f(0,1,0));
		setPhysicsLocation(new Vector3f(0, 20, 0));
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
		}
		setWalkDirection(walkDirection);
	}
}
