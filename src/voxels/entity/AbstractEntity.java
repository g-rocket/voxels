package voxels.entity;

import com.jme3.math.*;

public abstract class AbstractEntity implements Entity {
	protected Vector3f location;
	protected Vector3f velocity;
	
	protected float collisionHeight;
	protected float collisionRadius;
	protected float mass;
	
	public AbstractEntity(float collisionHeight, float collisionRadius, float mass) {
		this.collisionHeight = collisionHeight;
		this.collisionRadius = collisionRadius;
		this.mass = mass;
		
		location = new Vector3f();
		velocity = new Vector3f();
	}

	@Override
	public Vector3f getLocation() {
		return location;
	}

	@Override
	public Vector3f getVelocity() {
		return velocity;
	}

	@Override
	public void setLocation(Vector3f location) {
		this.location.set(location);
	}

	@Override
	public void setVelocity(Vector3f velocity) {
		this.velocity.set(velocity);
	}

	@Override
	public float getCollisionHeight() {
		return collisionHeight;
	}

	@Override
	public float getCollisionRadius() {
		return collisionRadius;
	}
	
	@Override
	public float getMass() {
		return mass;
	}

}
