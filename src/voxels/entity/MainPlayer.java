package voxels.entity;

import java.util.*;

import voxels.map.*;

import com.jme3.input.controls.*;
import com.jme3.math.*;
import com.jme3.renderer.*;
import com.jme3.scene.*;
import com.jme3.scene.control.CameraControl.ControlDirection;

public class MainPlayer extends AbstractEntity implements Player, ActionListener, AnalogListener {
	private final Camera camera;
	private WorldMap map;
	
	private Node playerNode;
	private final CameraNode cameraNode;

	private float frontForce = 8f;
	private float sideForce = 4f;
	private float backForce = 2f;
	
	private float flySpeedFactor = 0.5f;
	
	private Map<String,Boolean> actionsActive = new HashMap<>();

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
	public Vector3f getCustomForces(float dt) {
		Vector3f force = super.getCustomForces(dt);
		if(actionsActive.containsKey("Left")) force.addLocal(camera.getLeft().mult(sideForce));
		if(actionsActive.containsKey("Right")) force.addLocal(camera.getLeft().mult(-sideForce));
		if(actionsActive.containsKey("Forward")) force.addLocal(camera.getDirection().clone().setZ(0).mult(frontForce));
		if(actionsActive.containsKey("Backward")) force.addLocal(camera.getDirection().clone().setZ(0).mult(-backForce));
		if(!wasOnGround()) force.multLocal(flySpeedFactor);

		if(actionsActive.containsKey("Jump")){
			if(wasOnGround()) {
				System.out.print("j");
				force.addLocal(new Vector3f(0,0,5f).divide(dt));
			}
		}
		
		return force;
	}

	@Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if(isPressed) {
			actionsActive.put(name,actionsActive.containsKey(name));
		} else {
			actionsActive.remove(name);
		}
	}

	@Override
	public void onAnalog(String name, float value, float tpf) {
		
	}
}
