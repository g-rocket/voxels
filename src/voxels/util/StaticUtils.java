package voxels.util;

public class StaticUtils {
	private StaticUtils(){} // Should not be instantiated
	
	public static int mod(int i, int m) {
		return i % m + ((i % m) < 0? m: 0);
	}
	
	public static float mod(float i, float m) {
		return i % m + ((i % m) < 0? m: 0);
	}
	
	public static int ifdiv(int n, int d) {
	    if (n >= 0) return n / d;
	    else return ~(~n / d);
	}
}
