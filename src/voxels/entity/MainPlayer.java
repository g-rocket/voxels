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

	private float sideSpeed = 1;
	private float frontSpeed = 2;
	private float backSpeed = .5f;

	public MainPlayer(Camera camera, Node rootNode, WorldMap map) {
		super(.5f, 1.8f, 1f);
		
		camera.setAxes(Vector3f.UNIT_Y, Vector3f.UNIT_Z, Vector3f.UNIT_X);
		playerNode = new Node("Main Player");
		rootNode.attachChild(playerNode);
		
		setLocation(new Vector3f(.5f,.5f,50.5f));
		
		CameraNode cameraNode = new CameraNode("Main Player camera", camera);
		playerNode.attachChild(cameraNode);
		cameraNode.getLocalTransform().setRotation(camera.getRotation());
		//cameraNode.getLocalTransform().setTranslation(camera.getLocation());
		cameraNode.setControlDir(ControlDirection.SpatialToCamera);
		cameraNode.lookAt(playerNode.getLocalTranslation().add(3, 0, 1), Vector3f.UNIT_Z);
		this.camera = camera;
		this.map = map;
	}
	
	@Override
	public void setLocation(Vector3f location) {
		super.setLocation(location);
		playerNode.getLocalTransform().setTranslation(location);
	}
	
	public void setMap(WorldMap map) {
		this.map = map;
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		Vector3f walkDirection = new Vector3f(0,0,0);
		if(name.equals("Left")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(sideSpeed));
		if(name.equals("Right")) if(isPressed) walkDirection.addLocal(camera.getLeft().mult(-sideSpeed));
		if(name.equals("Forward")) if(isPressed) walkDirection.addLocal(camera.getDirection().clone().setZ(0).mult(frontSpeed));
		if(name.equals("Backward")) if(isPressed) walkDirection.addLocal(camera.getDirection().clone().setZ(0).mult(-backSpeed));
		velocity.addLocal(walkDirection);
		if(name.equals("Jump")) if(isPressed) velocity.addLocal(new Vector3f(0,0,5));
	}
}
