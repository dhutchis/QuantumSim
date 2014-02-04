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
		Complex c0 = new Complex(0,Math.sqrt(0.5));
		Complex c1 = new Complex(Math.sqrt(0.75),0);
		
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),2);
		vec.setEntry(0, c0);
		vec.setEntry(1, c1);
		
		FieldVector<Complex> outvec = new Z().apply(vec);
		
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(0), c0)  );
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(1), c1.negate())  );
	}
	
	@Test
	public void testCNOT() {
		Complex c0 = new Complex(0,1/3.0),
				c1 = new Complex(Math.sqrt(1/6.0),0),
				c2 = new Complex(2/3.0,0),
				c3 = new Complex(0,Math.sqrt(5/18.0));
		
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),4);
		vec.setEntry(0, c0);
		vec.setEntry(1, c1);
		vec.setEntry(2, c2);
		vec.setEntry(3, c3);
		
		FieldVector<Complex> outvec = new CNOT().apply(vec);
		
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(0), c0)  );
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(1), c1)  );
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(2), c3)  );
		assertTrue( QuantumUtil.isApproxEqualComplex(outvec.getEntry(3), c2)  );
	}

}
