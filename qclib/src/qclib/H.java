package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class H extends Operator {

	public H(int arity) {
		super(1);
	}
	
	/** 
	 * a|0> + b|1> => a[ |0> + |1> ]/sqrt(2) + b[ |0> - |1> ]/sqrt(2)
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(0, invec.getEntry(0).add(invec.getEntry(1)).divide(Math.sqrt(2)) );
		outvec.setEntry(0, invec.getEntry(0).subtract(invec.getEntry(1)).divide(Math.sqrt(2)) );
		return outvec;
	}

}
