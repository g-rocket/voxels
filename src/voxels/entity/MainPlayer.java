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

	private float sideSpeed = 1;
	private float frontSpeed = 2;
	private float backSpeed = .5f;

	public MainPlayer(Camera camera, Node rootNode, WorldMap map) {
		super(.5f, 1.8f, 1f);
		
		camera.setAxes(new Vector3f(0,1,0), new Vector3f(0,0,1), new Vector3f(1,0,0));
		Node playerNode = new Node("Main Player");
		rootNode.attachChild(playerNode);
		
		setLocation(new Vector3f(.5f,.5f,50.5f));
		
		
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
	public void onAction(String name, boolean isPressed, float tpf) {
		Vector3f walkDirection = new Vector3f(0,0,0);
		if(name.equals("Left")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(sideSpeed));
		if(name.equals("Right")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(-sideSpeed));
		if(name.equals("Up")) if(isPressed) walkDirection.addLocal(camera.getDirection().setZ(0).mult(frontSpeed));
		if(name.equals("Down")) if(isPressed) walkDirection.addLocal(camera.getDirection().setZ(0).mult(-backSpeed));
		//setWalkDirection(walkDirection);
		//if(name.equals("Jump")) if(isPressed) super.jump();
	}
}
