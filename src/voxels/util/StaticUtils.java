package voxels.util;

public class StaticUtils {
	private StaticUtils(){} // Should not be instantiated
	
	public static int mod(int i, int m) {
		return i % m + ((i % m) < 0? m: 0);
	}
	
	public static float mod(float i, float m) {
		return i % m + ((i % m) < 0? m: 0);
	}
	
	public static int modb2(int i, int logm) {
		return i & ((1<<logm) - 1);
	}
	
	public static int ifdiv(int n, int d) {
	    if (n >= 0) return n / d;
	    else return ~(~n / d);
	}
	
    public static int logb2(int n) { // assumes positive integer power of two
    	for(int i = 30; i >= 0; i--) {
    		if((1 << i) == n) return i;
    	}
    	throw new IllegalArgumentException("i is not a positive integer power of two");
    }
}
