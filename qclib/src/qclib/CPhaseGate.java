package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

public class CPhaseGate extends Operator {
	private double phaseShift;
	
	public CPhaseGate(double phaseShift) {
		super(2);
		this.phaseShift = phaseShift;
	}

	/**
	 *     a|00> + b|01> + c|10> + d|11>
	 * ==> a|00> + b|01> + c|10> + d*e^(i*phaseShift)|11>
	 * Creates new vector; does not change original.
	 */
	@Override
	public FieldVector<Complex> apply(FieldVector<Complex> invec) {
		FieldVector<Complex> outvec = invec.copy();
		outvec.setEntry(3, outvec.getEntry(3).multiply(new Complex(0,this.phaseShift).exp()));
		return outvec;
	}

}
