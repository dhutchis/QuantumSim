package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class Z extends Operator {

	public Z() {
		super(1);
	}

	/**
	 * a|0> + b|1> ==> a|0> - b|1>
	 * Creates new vector; does not change original.
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(1, outvec.getEntry(1).negate());
		return outvec;
	}

}
