package qclib;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.util.QuantumUtil;

public class ZTest {

	@Test
	public void testApply() {
		Complex c0 = new Complex(0,Math.sqrt(0.5));
		Complex c1 = new Complex(Math.sqrt(0.75),0);
		
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),2);
		vec.setEntry(0, c0);
		vec.setEntry(1, c1);
		
		FieldVector<Complex> outvec = new Z().apply(vec);
		
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(0), c0)  );
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(1), c1.negate())  );
	}

}
