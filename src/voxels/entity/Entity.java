package voxels.entity;

import voxels.map.*;

import com.jme3.math.*;

public interface Entity {
	public Vector3f getLocation();
	public Vector3f getVelocity();
	
	public void setLocation(Vector3f location);
	public void setVelocity(Vector3f velocity);
	
	public float getCollisionHeight();
	public float getCollisionRadius();
	public float getMass();
	
	public boolean wasOnGround();
	public void setOnGround(boolean onGround);
	
	public default Vector3f getCustomForces() {
		return Vector3f.ZERO;
	}
	
	public default void applyForce(Vector3f force) {
		setVelocity(force.mult(getMass()).addLocal(getVelocity()));
	}
	
	public default Vector3f getNextLocation(float dt) {
		return getVelocity().mult(dt).addLocal(getLocation());
	}
	
	public default void applyVelocity(float dt) {
		setLocation(getNextLocation(dt));
	}
	
	public default Coord3 getBlockLocation() {
		return new Coord3(getLocation());
	}
}
