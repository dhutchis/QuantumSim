package qclib.op;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

public class V extends Operator {

	public V() {
		super(1);
	}

	/**
	 * a|0> + b|1> ==> a|0> + i*b|1>
	 * Creates new vector; does not change original.
	 */
	@Override
	public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(1, outvec.getEntry(1).multiply(Complex.I));
		return outvec;
	}

}
