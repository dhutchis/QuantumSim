package qclib.alg;

public interface FunctionDeutsch {
	/** Return true for 1, false for 0.  Input ranges from 0 to 2^N-1.
	 * REQUIRES BALANCED OR CONSTANT FUNCTION. */
	public boolean apply(int input);
}