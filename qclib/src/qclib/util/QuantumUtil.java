package qclib.util;

public class QuantumUtil {
	
	/** Tolerance for double equality checking */
	public final static double EPSILON = 0.00001;
	public static boolean isApproxZero(double arg) {
		return arg < EPSILON && arg > -EPSILON;
	}

}
