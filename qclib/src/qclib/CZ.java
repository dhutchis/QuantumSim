package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class CZ extends Operator {

	public CZ() {
		super(2);
	}

	/**
	 *     a|00> + b|01> + c|10> + d|11>
	 * ==> a|00> + b|01> + c|10> - d|11>
	 * Creates new vector; does not change original.
	 * First bit is target bit; second bit is control bit.
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(3, outvec.getEntry(3).negate());
		return outvec;
	}

}
