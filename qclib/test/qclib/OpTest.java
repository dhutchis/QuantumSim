package qclib;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.util.QuantumUtil;

public class OpTest {

	@Test
	public void testZ() {
		Complex c0 = new Complex(0,Math.sqrt(0.5)),
				c1 = new Complex(Math.sqrt(0.75),0);
		FieldVector<Complex> vec = QuantumUtil.buildVector(c0, c1);
		FieldVector<Complex> outvec = new Z().apply(vec);
		assertTrue( QuantumUtil.isApproxEqualVector(outvec, vec)  );
	}
	
	@Test
	public void testCNOT() {
		Complex c0 = new Complex(0,1/3.0),
				c1 = new Complex(Math.sqrt(1/6.0),0),
				c2 = new Complex(2/3.0,0),
				c3 = new Complex(0,Math.sqrt(5/18.0));
		FieldVector<Complex> vec = QuantumUtil.buildVector(c0, c1, c2, c3);;
		FieldVector<Complex> outvec = new CNOT().apply(vec);
		assertTrue( QuantumUtil.isApproxEqualVector(outvec, vec)  );
	}

}
