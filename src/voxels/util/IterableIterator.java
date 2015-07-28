package voxels.util;

import java.util.*;

public class IterableIterator<T> implements Iterable<T> {
	private Iterator<T> i;
	
	public IterableIterator(Iterator<T> i){
		this.i = i;
	}

	@Override
	public Iterator<T> iterator() {
		return i;
	}

}
