package qclib.op;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

public class CV extends Operator {

	/**
	 *  First bit is the control bit, the second is the target bit.
	 */
	public CV() {
		super(2);
	}

	/**
	 *     a|00> + b|01> + c|10> + d|11>
	 * ==> a|00> + b|01> + c|10> + i*d|11>
	 * Creates new vector; does not change original.
	 * Order of the two qubits does not matter.
	 */
	@Override
	public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(3, outvec.getEntry(3).multiply(Complex.I));
		return outvec;
	}

}
