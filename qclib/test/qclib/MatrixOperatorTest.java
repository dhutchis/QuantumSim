/**
 * 
 */
package qclib;

import static org.junit.Assert.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.BlockFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrix;
import org.junit.Test;

import qclib.util.QuantumUtil;

/**
 * @author dhutchis
 *
 */
public class MatrixOperatorTest {

	/**
	 * Test method for {@link qclib.MatrixOperator#myApply(org.apache.commons.math3.linear.FieldVector)}.
	 */
	@Test
	public final void testMyApply() {
		// x|0> + y|1> ==> y|0> - x|1>
		FieldMatrix<Complex> swapNeg = new BlockFieldMatrix<Complex>(new Complex[][] {
				new Complex[] { Complex.ZERO, Complex.ONE },
				new Complex[] { Complex.ONE.negate(), Complex.ZERO }
		});
		
		Operator op = new MatrixOperator(1, swapNeg);
		
		QubitRegister qr = new QubitRegister(15);
		qr.setAmps(QuantumUtil.buildVector(1,2), 10);
		System.out.println(qr.printBits(10));
		System.out.println("After swapNeg operation on qubit 10");
		qr.doOp(op, 10);
		System.out.println(qr.printBits(10));
		
		assertEquals(QuantumUtil.buildVector(2, -1) , qr.getAmps(10));
	}

	/**
	 * Test method for {@link qclib.MatrixOperator#curryBefore(qclib.Operator)}.
	 */
	@Test
	public final void testCurryBefore() {
		// now test that currying MatrixOperators has the same effect
		Operator negFirst = new MatrixOperator(1, new BlockFieldMatrix<Complex>(new Complex[][] {
				new Complex[] { Complex.ONE.negate(), Complex.ZERO },
				new Complex[] { Complex.ZERO, Complex.ONE }
		}));
		Operator swap = new MatrixOperator(1, new BlockFieldMatrix<Complex>(new Complex[][] {
				new Complex[] { Complex.ZERO, Complex.ONE },
				new Complex[] { Complex.ONE, Complex.ZERO }
		}));
		Operator combo = negFirst.curryBefore(swap);
		
		// test it
		QubitRegister qr = new QubitRegister(15);
		qr.setAmps(QuantumUtil.buildVector(1,2), 10);
		System.out.println(qr.printBits(10));
		System.out.println("After combo operation on qubit 10");
		qr.doOp(combo, 10);
		System.out.println(qr.printBits(10));
		
		assertEquals(QuantumUtil.buildVector(2, -1) , qr.getAmps(10));
	}

}
