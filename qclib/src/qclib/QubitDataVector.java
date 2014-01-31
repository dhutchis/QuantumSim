package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

public class QubitDataVector extends QubitDataAbstract {
	/** the data holder */
	private FieldVector<Complex> data;

	public QubitDataVector(int numbits) {
		super(numbits);
		data = new ArrayFieldVector<Complex>(ComplexField.getInstance(),numbits);
	}

	@Override
	protected Complex[] customGetAmp(int... bits) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void customSetAmp(int[] bits, Complex[] amps) {
		// TODO Auto-generated method stub

	}

}
