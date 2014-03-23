package qclib.alg;

public interface FunctionFilter {
	/** Return true for 1, false for 0.  Input ranges from 0 to 2^N-1.  */
	public boolean apply(int input);
}