package voxels.entity;

import voxels.map.*;

import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.CameraControl.ControlDirection;

public class MainPlayer extends AbstractEntity implements Player, ActionListener {
	private final Camera camera;
	private WorldMap map;
	
	private Node playerNode;
	private final CameraNode cameraNode;

	private float sideForce = .02f;
	private float frontForce = .04f;
	private float backForce = .01f;
	
	private float flySpeedFactor = 0.5f;
	
	private Vector3f walkDirection = Vector3f.ZERO.clone();

	public MainPlayer(Camera camera, Node rootNode, WorldMap map) {
		super(.5f, 1.8f, 1f);
		
		camera.setAxes(Vector3f.UNIT_Y, Vector3f.UNIT_Z, Vector3f.UNIT_X);
		playerNode = new Node("Main Player");
		rootNode.attachChild(playerNode);
		
		cameraNode = new CameraNode("Main Player camera", camera);
		playerNode.attachChild(cameraNode);
		cameraNode.getLocalTransform().setRotation(camera.getRotation());
		//cameraNode.getLocalTransform().setTranslation(camera.getLocation());
		cameraNode.setControlDir(ControlDirection.SpatialToCamera);
		cameraNode.lookAt(playerNode.getLocalTranslation().add(1,0,0), Vector3f.UNIT_Z.clone());
		this.camera = camera;
		this.map = map;
		
		setLocation(new Vector3f(.5f,.5f,50.5f));
	}
	
	@Override
	public void setLocation(Vector3f location) {
		super.setLocation(location);
		playerNode.getLocalTransform().setTranslation(location);
		cameraNode.getWorldTransform().setTranslation(location.add(0,0,1));
		//System.out.printf("Camera transform:\n%s\n",cameraNode.getLocalTransform());
		//System.out.printf("Camera global transform:\n%s\n", cameraNode.getWorldTransform());
	}
	
	public void setMap(WorldMap map) {
		this.map = map;
	}
	
	@Override
	public Vector3f getCustomForces() {
		return super.getCustomForces().add(walkDirection);
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		walkDirection = Vector3f.ZERO.clone();
		if(name.equals("Left")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(sideForce));
		if(name.equals("Right")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(-sideForce));
		if(name.equals("Forward")) if(isPressed) walkDirection.addLocal(camera.getDirection().clone().setZ(0).mult(frontForce));
		if(name.equals("Backward")) if(isPressed) walkDirection.addLocal(camera.getDirection().clone().setZ(0).mult(-backForce));
		if(!wasOnGround()) walkDirection.multLocal(flySpeedFactor);
		if(name.equals("Jump")) if(isPressed && wasOnGround()) velocity.addLocal(new Vector3f(0,0,5));
	}
}
