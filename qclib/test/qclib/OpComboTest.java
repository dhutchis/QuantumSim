/**
 * 
 */
package qclib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.op.CNOT;
import qclib.op.CV;
import qclib.op.H;
import qclib.op.Z;
import qclib.util.QuantumUtil;

/**
 *
 */
public class OpComboTest {

	/**
	 * Test method for {@link qclib.Operator#curryBefore(qclib.Operator)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public final void testCurryAfter() {
		// do cnot(control=0,target=1), then z(1), then cnot(control=0,target=1)
		Operator z0=new Z(), cnot01 = new CNOT(); // target,control = 0,1
		Operator cnot10 = cnot01.swapBits(1, 0);  // target,control = 1,0
		Operator z1 = z0.extend(2, 1);
		Operator combo = cnot01.curryBefore(z1).curryBefore(cnot10);
		
		final int numtests = 5;
		FieldVector<Complex>[] v = new FieldVector[numtests];
		FieldVector<Complex>[] ve = new FieldVector[numtests];
		Operator[] o = new Operator[] {z1, z1, cnot10, cnot01, combo};
		int[][] tbs = new int[numtests][];
		int[] dvll = new int[] {2,2,2,2,2};
		
		v[0] =  QuantumUtil.buildVector(0b00, 0b01, 0b10, 0b11);
		ve[0] = QuantumUtil.buildVector(0b00, 0b01, -0b10, -0b11);
		tbs[0] = new int[] {0,1};
		
		v[1] =  QuantumUtil.buildVector(0b00, 0b01, 0b10, 0b11);
		ve[1] = QuantumUtil.buildVector(0b00, -0b01, 0b10, -0b11);
		tbs[1] = new int[] {1,0};
		
		v[2] =  QuantumUtil.buildVector(0b00, 0b01, 0b10, 0b11);
		ve[2] = QuantumUtil.buildVector(0b00, 0b11, 0b10, 0b01);
		tbs[2] = new int[] {0,1};
		
		v[3] =  QuantumUtil.buildVector(0b00, 0b01, 0b10, 0b11);
		ve[3] = QuantumUtil.buildVector(0b00, 0b01, 0b11, 0b10);
		tbs[3] = new int[] {0,1};
		
		v[4] =  QuantumUtil.buildVector(0b00, 0b01, 0b10, 0b11);
		//								0b00, 0b01, 0b11, 0b10   // after cnot01
		//								0b00, 0b01, -0b11,-0b10  // after z1
		ve[4] = QuantumUtil.buildVector(0b00, -0b10,-0b11,0b01); // after cnot10
		tbs[4] = new int[] {0,1};
		
		for (int t=0; t<numtests; t++) {
			o[t].applyTo(dvll[t], v[t], tbs[t]);
			assertTrue("testcase["+t+"]:\nresult ="+QuantumUtil.printVector(v[t])+"\nexpected="+QuantumUtil.printVector(ve[t]),
					QuantumUtil.isApproxEqualVector(v[t], ve[t]) );
		}
	}

	/**
	 * Test method for {@link qclib.Operator#combineIndependentOps(int, java.util.Map)}.
	 */
	@Test
	public final void testExtend() {
		// create a 2-qubit operator ZZ that applies Z to both qubits
		Operator z=new Z();
		Map<Operator,List<int[]>> opmap = new HashMap<Operator,List<int[]>>(2);
		opmap.put(z, Arrays.asList( new int[][] {{0},{1}} ));
		Operator zz = z.extend(2, 0).curryBefore(z.extend(2, 1));
		
		FieldVector<Complex> v1, v1e;
		v1 = QuantumUtil.buildVector(10,11,12,13);
		//							 10,-11,12,-13 // after Z on bit 0
		v1e = QuantumUtil.buildVector(10,-11,-12,13); // after Z on bit 1
		zz.applyTo(2, v1, new int[] {1,0});

		assertTrue("result ="+QuantumUtil.printVector(v1)+"\nexpected="+QuantumUtil.printVector(v1e),
				QuantumUtil.isApproxEqualVector(v1e, v1) );
	}
	
	@Test
	public final void testReorderBits() {
		QubitContainer qc = new QubitContainer(3);
		qc.setAmps(QuantumUtil.buildVector(0, 1, 2, 3, 4, 5, 6, 7));
		qc.reorderBits(2,0,1);
		FieldVector<Complex> v = qc.getAmps();
		FieldVector<Complex> ve = QuantumUtil.buildVector(0b000,0b100,0b001,0b101,0b010,0b110,0b011,0b111 );
		assertEquals(ve,v);
	}

}
