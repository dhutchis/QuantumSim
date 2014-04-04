package qclib.op;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

public class X extends Operator {

	public X() {
		super(1);
	}

	@Override
	public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(0, invec.getEntry(1));
		outvec.setEntry(1, invec.getEntry(0));
		return outvec;
	}

}
