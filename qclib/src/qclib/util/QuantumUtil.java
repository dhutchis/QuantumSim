package qclib.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;

public class QuantumUtil {
	
	/** Tolerance for double equality checking */
	public final static double EPSILON = 0.00001;
	public final static boolean isApproxZero(double arg) {
		return arg < EPSILON && arg > -EPSILON;
	}
	
	public final static double square(double a) { return a*a; }

	public final static int[] integerToIntArray(Integer[] in) {
		int[] ret = new int[in.length];
		for (int i=0; i<in.length; i++)
			ret[i] = in[i];
		return ret;
	}
	
	public final static boolean isApproxEqualComplex(Complex x, Complex y) {
		return isApproxZero(x.getReal()-y.getReal()) &&
				isApproxZero(x.getImaginary()-y.getImaginary());
	}
	
	
	/// ---------------
	/// Index Functions
	/// ---------------
	
	public static final int getLowestBit(int a) {
		return ((a-1)&a)^a;
	}
	
	/**
	 * 
	 * @param v1loglen
	 * @param targetbits
	 * @return
	 */
	public static Set<int[]> translateIndices(int v1loglen, int... targetbits) {
		if (v1loglen < 2 || targetbits == null || targetbits.length > v1loglen)
			throw new IllegalArgumentException("bad v1loglen="+v1loglen+", targetbits "+targetbits+(targetbits==null?"":" with length "+targetbits));

		// running example: v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {2,0}, targetbits.length = 2
		//int[][] ret = new int[1<<(v1loglen-targetbits.length)][1<<(targetbits.length)];
		Set<int[]> ret = new HashSet<int[]>(1<<(v1loglen-targetbits.length));
		// ret is a (2^1)x(2^2) = 2x4 array
		// expected result:	{{000, 100, 001, 101},
		//					 {010, 110, 011, 111}} // order of the two arrays doesn't matter
		int freebitmask = (1<<v1loglen)-1;
		// freebitmask = (2^3-1) = (1000 - 1) = 111
		// now change it to 010, since bit 1 is free, meaning not specified in targetbits
		for (int tb : targetbits)
			freebitmask ^= 1<<tb; // 111 ^ 100 = 011, 011 ^ 001 = 010
		//for each free bit: 
		for (int i = freebitmask; i != 0; ) {
			int freebit = getLowestBit(i);
			i &= ~freebit; // clear lowest bit
		}
	}
	
	
}
