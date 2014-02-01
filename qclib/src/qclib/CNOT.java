package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class CNOT extends Operator {

	public CNOT() {
		super(2);
	}

	/**
	 *     a|00> + b|01> + c|10> + d|11>
	 * ==> a|00> + b|01> + d|10> + c|11>
	 * Creates new vector; does not change original.
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(2, invec.getEntry(3));
		outvec.setEntry(3, invec.getEntry(2));
		return outvec;
	}

}
