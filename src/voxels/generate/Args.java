package voxels.generate;

class Args {
	public final Class<?>[] types;
	public final Object[] values;
	
	public Args(Class<?>[] types, Object[] values) {
		this.types = types;
		this.values = values;
	}

	public Args(Value arg) {
		this.types = new Class[]{arg.type};
		this.values = new Object[]{arg.value};
	}
}