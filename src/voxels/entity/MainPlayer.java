package voxels.entity;

import voxels.map.*;

import com.jme3.bullet.*;
import com.jme3.bullet.control.*;
import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.CameraControl.ControlDirection;

public class MainPlayer extends BetterCharacterControl implements Player, ActionListener {
	private final Camera camera;
	private WorldMap map;

	private float sideSpeed = 1;
	private float frontSpeed = 2;
	private float backSpeed = .5f;

	public MainPlayer(Camera camera, Node rootNode, WorldMap map) {
		super(.5f, 1.8f, 1f);
		camera.setAxes(new Vector3f(0,1,0), new Vector3f(0,0,1), new Vector3f(1,0,0));
		Node playerNode = new Node("Main Player");
		rootNode.attachChild(playerNode);
		playerNode.addControl(this);
		
		setJumpForce(new Vector3f(0, 0, 3));
		setGravity(new Vector3f(0,0,1));
		setPhysicsLocation(new Vector3f(0,0,50));
		
		CameraNode cameraNode = new CameraNode("Main Player camera", camera);
		cameraNode.setControlDir(ControlDirection.SpatialToCamera);
		playerNode.attachChild(cameraNode);
		this.camera = camera;
		this.map = map;
	}
	
	public void setMap(WorldMap map) {
		this.map = map;
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
		if(isSolid(0,0,0)) {
			Vector3f location = super.getSpatialTranslation();
			location.setY((float)Math.floor(location.getY() + 1));
			this.warp(location);
		}
	}
	
	@Override
	protected void checkOnGround() {
		onGround = isSolid(0,0,-1);
	}
	
	private boolean isSolid(int dx, int dy, int dz) {
		return isSolid(new Coord3(dx, dy, dz));
	}
	
	private boolean isSolid(Coord3 offset) {
		return map.getBlock(getBlockLocation().plus(offset)).isSolid;
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		Vector3f walkDirection = new Vector3f(0,0,0);
		if(name.equals("Left")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(sideSpeed));
		if(name.equals("Right")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(-sideSpeed));
		if(name.equals("Up")) if(isPressed) walkDirection.addLocal(camera.getDirection().mult(frontSpeed));
		if(name.equals("Down")) if(isPressed) walkDirection.addLocal(camera.getDirection().mult(-backSpeed));
		setWalkDirection(walkDirection);
		if(name.equals("Jump")) if(isPressed) super.jump();
	}
}
