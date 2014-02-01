package qclib.util;

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
}
