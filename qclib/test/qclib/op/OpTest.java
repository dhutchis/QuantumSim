package qclib.op;

import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.complex.Complex;
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
		FieldVector<Complex> testvec = QuantumUtil.buildVector(c0, c1.negate());
		assertTrue( QuantumUtil.isApproxEqualVector(outvec, testvec)  );
	}
	
	@Test
	public void testCNOT() {
		Complex c0 = new Complex(0,1/3.0),
				c1 = new Complex(Math.sqrt(1/6.0),0),
				c2 = new Complex(2/3.0,0),
				c3 = new Complex(0,Math.sqrt(5/18.0));
		FieldVector<Complex> vec = QuantumUtil.buildVector(c0, c1, c2, c3);;
		FieldVector<Complex> outvec = new CNOT().apply(vec);
		FieldVector<Complex> testvec = QuantumUtil.buildVector(c0, c1, c3, c2);
		assertTrue( QuantumUtil.isApproxEqualVector(outvec, testvec)  );
	}
	
	@Test
	public void testH() {
		Complex c0 = new Complex(1,3),
				c1 = new Complex(Math.sqrt(1/6.0),1);
		FieldVector<Complex> vec = QuantumUtil.buildVector(c0, c1);;
		FieldVector<Complex> outvec = new H().apply(vec);
		FieldVector<Complex> testvec = QuantumUtil.buildVector((c0.add(c1)).divide(Math.sqrt(2)), (c0.subtract(c1)).divide(Math.sqrt(2)));
		assertTrue( QuantumUtil.isApproxEqualVector(outvec, testvec)  );
	}
	
	@Test
	public void testToffoli() {
//		Complex c0 = new Complex(1,1),
//				c1 = new Complex(1,1),
//				c2 = new Complex(1,1);
		FieldVector<Complex> vec = QuantumUtil.buildVector(1, 0, 0, 0, 0, 0, 0, 0);
		FieldVector<Complex> outvec = ComboOps.toffoli().apply(vec);
		
		//FieldVector<Complex> testvec = QuantumUtil.buildVector();
		//assertTrue( QuantumUtil.isApproxEqualVector(outvec, testvec)  );
	}
	
	
}
