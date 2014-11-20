package voxels.util;

import java.util.*;

public class ListStartedList<E> extends AbstractList<E> {
	private final List<E>[] starters;
	private final int startersTotalSize;
	private final List<E> backing = new ArrayList<>();

	@SafeVarargs
	public ListStartedList(List<E>... starters) {
		this.starters = starters;
		startersTotalSize = size();
	}
	
	@Override
	public int size() {
		int ssize = 0;
		for(List<E> s: starters) ssize += s.size();
		return ssize + backing.size();
	}

	@Override
	public E get(int i) {
		for(List<E> s: starters) {
			if(i < s.size()) {
				return s.get(i);
			} else {
				i -= s.size();
			}
		}
		return backing.get(i);
	}
	
	@Override
	public E set(int i, E obj) {
		if(i < startersTotalSize) throw new UnsupportedOperationException();
		return backing.set(i - startersTotalSize, obj);
	}
	
	@Override
	public void add(int i, E obj) {
		if(i < startersTotalSize) throw new UnsupportedOperationException();
		backing.add(i - startersTotalSize, obj);
	}
	
	@Override
	public E remove(int i) {
		if(i < startersTotalSize) throw new UnsupportedOperationException();
		return backing.remove(i - startersTotalSize);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>(){
			private int pos = 0;
			@Override
			public boolean hasNext() {
				//System.out.println(size());
				return pos < size();
			}

			@Override
			public E next() {
				return get(pos++);
			}
			
		};
	}
}