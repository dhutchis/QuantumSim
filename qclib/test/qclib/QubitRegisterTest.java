/**
 * 
 */
package qclib;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.util.QuantumUtil;

/**
 * @author dhutchis
 *
 */
public class QubitRegisterTest {

	/**
	 * Test method for {@link qclib.QubitRegister#getAmps(int[])}.
	 * Test method for {@link qclib.QubitRegister#setAmps(org.apache.commons.math3.linear.FieldVector, int[])}.
	 */
	@Test
	public final void testAmps() {
		QubitRegister qr = new QubitRegister(4);
		qr.setAmps(QuantumUtil.buildVector(3.0/5, -4.0/5), 0);
		qr.setAmps(QuantumUtil.buildVector(3.0/5, 4.0/5), 1);
		qr.couple(0,1);
		
		FieldVector<Complex> e01, e10, v01, v10;
		e01 = QuantumUtil.buildVector( 9.0/25, -12.0/25, 12.0/25, -16.0/25 );
		e10 = QuantumUtil.buildVector( 9.0/25, 12.0/25, -12.0/25, -16.0/25 );
		
		v01 = qr.getAmps(0,1);
		v10 = qr.getAmps(1,0);
		
		assertTrue("result="+QuantumUtil.printVector(v01)+"\nexpected="+QuantumUtil.printVector(e01),
				QuantumUtil.isApproxEqualVector(e01, v01));
		assertTrue("result="+QuantumUtil.printVector(v10)+"\nexpected="+QuantumUtil.printVector(e10),
				QuantumUtil.isApproxEqualVector(e10, v10));
	}

	/**
	 * Test method for {@link qclib.QubitRegister#measure(int)}.
	 */
	@Test
	public final void testMeasure() {
		QubitRegister qr = new QubitRegister(4);
		FieldVector<Complex> v0, v1;
		v0 = QuantumUtil.buildVector(1, 0);
		v1 = QuantumUtil.buildVector(Complex.ZERO, Complex.I);
		
		qr.setAmps(v0, 0);
		qr.setAmps(v1, 1);
		
		assertFalse(qr.measure(0)); // always measures 0
		assertTrue(qr.measure(1));  // always measures 1
		
		// state of vectors should not have changed
		assertEquals( v0, qr.getAmps(0) );
		assertEquals( v1, qr.getAmps(1) );
		
		// Todo?: Measure more complicated vectors many times (say 10000), and test if average is close to what it should be.		
	}

}
