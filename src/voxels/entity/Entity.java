package voxels.entity;

import java.util.*;

import voxels.block.texture.*;

import com.jme3.math.*;

public abstract class Entity {
	protected Vector3f location;
	protected Vector3f velocity;
	
	protected float collisionHeight;
	protected float collisionRadius;
	protected float mass;
	
	protected Set<Direction> onSurfaces;
	
	public Entity(float collisionHeight, float collisionRadius, float mass) {
		this.collisionHeight = collisionHeight;
		this.collisionRadius = collisionRadius;
		this.mass = mass;
		
		onSurfaces = new HashSet<>();
		
		location = new Vector3f();
		velocity = new Vector3f();
	}

	public float getCollisionHeight() {
		return collisionHeight;
	}

	public float getCollisionRadius() {
		return collisionRadius;
	}
	
	
	
	public float getMass() {
		return mass;
	}
	
	

	public Vector3f getLocation() {
		return location;
	}
	
	public Vector3f getNextLocation(float dt) {
		return getVelocity().mult(dt).addLocal(getLocation());
	}

	public void setLocation(Vector3f location) {
		this.location.set(location);
	}
	
	

	public Vector3f getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector3f velocity) {
		this.velocity.set(velocity);
	}
	
	public void applyVelocity(float dt) {
		setLocation(getNextLocation(dt));
	}
	
	
	
	public Vector3f getCustomForces(float dt) {
		return Vector3f.ZERO.clone();
	}
	
	public void applyForce(Vector3f force, float dt) {
		setVelocity(force.mult(getMass()).multLocal(dt).addLocal(getVelocity()));
	}
	
	

	public boolean onSurface(Direction surface) {
		return onSurfaces.contains(surface);
	}
	
	public Set<Direction> onSurfaces() {
		return onSurfaces;
	}

	public void setOnSurface(Direction surface, boolean onSurface) {
		if(onSurface) {
			onSurfaces.add(surface);
		} else {
			onSurfaces.remove(surface);
		}
	}
}
