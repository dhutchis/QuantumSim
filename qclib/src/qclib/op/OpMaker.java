package qclib.op;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.util.QuantumUtil;

public class OpMaker {
	
	/**
	 * Returns an X operator, which switches the |0> and |1> component.
	 * @return
	 */
	public static Operator makeX() {
		return new Operator(1) {

			@Override
			protected FieldVector<Complex> myApply(FieldVector<Complex> invec) {
				FieldVector<Complex> outvec = QuantumUtil.buildVector(invec.getEntry(1), invec.getEntry(0));
				return outvec;
			}
			
		};
	}
}
