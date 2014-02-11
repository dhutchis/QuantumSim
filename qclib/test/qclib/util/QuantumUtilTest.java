/**
 * 
 */
package qclib.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.junit.Test;

import qclib.Operator;
import qclib.op.CNOT;
import qclib.op.Z;

/**
 *
 */
public class QuantumUtilTest {

	/**
	 * Test method for {@link qclib.util.QuantumUtil#doForEachFreeBitSet(java.util.BitSet, qclib.util.QuantumUtil.BitSetCallback)}.
	 */
	@Test
	public final void testDoForEachFreeBitSet() {
		BitSet freebitmask = new BitSet(4);
		freebitmask.set(0);
		freebitmask.set(2); // 0101
		Set<BitSet> expected = new HashSet<BitSet>(BitSetUtil.toBitSetListDeep(
				new int[][] { 
						{},
						{0},
						{2},
						{0,2}}));
		QuantumUtil.BitSetCallback<BitSet> callback = new QuantumUtil.BitSetCallback<BitSet>() {
			@Override
			public BitSet doWithFreeBitsSet(BitSet freebitset) {
				return freebitset;
			}
		};
		Set<BitSet> result = QuantumUtil.doForEachFreeBitSet(freebitmask, callback);
		assertEquals(expected, result);
	}

	/**
	 * Test method for {@link qclib.util.QuantumUtil#translateIndices(int, int[])}.
	 */
	@Test
	public final void testTranslateIndices() {
		/*
		 *  Translate indices on a vector v1 of length 2^v1loglen
	 		to indices on a vector v2 of length 2^targetbits.length
	 		where the indices on v2 correspond to the indices on v1 as specificed by targetbits.
	 
			 Examples:
			 v1 = {0, 1}, v1loglen = 1, targetbits = {0}, targetbits.length = 1
			 Returns: Set({0, 1}) // corresponds to {0, 1} of v2
			 
			 v1 = {00, 01, 10, 11}, v1loglen = 2, targetbits = {0, 1}, targetbits.length = 2
			 Returns: Set({00, 01, 10, 11}) // corresponds to {00, 01, 10, 11} of v2
			 
			 v1 = {00, 01, 10, 11}, v1loglen = 2, targetbits = {1, 0}, targetbits.length = 2
			 Returns: Set({00, 10, 01, 11}) // corresponds to {00, 01, 10, 11} of v2
			 
			 v1 = {00, 01, 10, 11}, v1loglen = 2, targetbits = {1}, targetbits.length = 2
			 Returns: Set({00, 10}, 	// corresponds to {0, 1} of v2
			 				{01, 11}}	// corresponds to {0, 1} of v2
			 
			 v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {2,0}, targetbits.length = 2
			 Returns: Set({000, 100, 001, 101},  //corresponds to {00, 01, 10, 11} of v2
			 			 	{010, 110, 011, 111})  //corresponds to {00, 01, 10, 11} of v2
			 
			 v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {1}, targetbits.length = 1
			 Returns: Set({000, 010},  //corresponds to {0, 1} of v2
			 				{001, 011},  //corresponds to {0, 1} of v2
			 				{100, 110},  //corresponds to {0, 1} of v2
			 			 	{101, 111})  //corresponds to {0, 1} of v2
			 
			 v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {0,2,1}, targetbits.length = 3
			 Returns: Set({000, 001, 100, 101, 010, 011, 110, 111})
			 Note: v1 and v2 have same sizes, but we reordered the indices.
		 */
		// test cases
		int[] v1loglen = new int[] {1,2,2,2,3,3,3};
		int[][] targetbits = new int[][] {{0}, {0,1}, {1,0}, {1}, {2,0}, {1}, {0,2,1} };
		
		@SuppressWarnings("unchecked")
		Set<int[]>[] expected = new Set[] {
				Collections.singleton(new int[] {0}),
				Collections.singleton(new int[] {0b00, 0b01, 0b10, 0b11}),
				Collections.singleton(new int[] {0b00, 0b10, 0b01, 0b11}),
				new HashSet<int[]>( Arrays.asList(new int[][] 
						{{0b00, 0b10},
						 {0b01, 0b11}}) ),
				new HashSet<int[]>( Arrays.asList(new int[][] 
						{{0b000, 0b100, 0b001, 0b101},
						 {0b010, 0b110, 0b011, 0b111}}) ),
				new HashSet<int[]>( Arrays.asList(new int[][] 
						{{0b000, 0b010},
						 {0b001, 0b011},
						 {0b100, 0b110},
						 {0b101, 0b111}}) ),
				Collections.singleton(new int[] {0b000, 0b001, 0b100, 0b101, 0b010, 0b011, 0b110, 0b111})
		};
		
		// Warning: THIS FAILS: assertEquals(new int[] {0}, new int[] {0});
		// for each test case
		for (int i=0; i < v1loglen.length; i++) {
			
			Set<int[]> result = QuantumUtil.translateIndices(v1loglen[i], targetbits[i]);
			// careful comparing int[] arrays.  Can only test equality.
//			Set<Integer[]> result2 = intToIntegerArraySet(result),
//					expected2 = intToIntegerArraySet(expected[i]);
			assertTrue("testcase["+i+"]:\nresult ="+BitSetUtil.printIntArraySet(result)+"\nexpected="+BitSetUtil.printIntArraySet(expected[i]),
					isEqualSetOfIntArray(expected[i], result));
		}
	}
	
	/*private static Set<Integer[]> intToIntegerArraySet(Set<int[]> in) {
		Set<Integer[]> out = new HashSet<Integer[]>(in.size());
		for (int[] iarr : in)
			out.add(QuantumUtil.intToIntegerArray(iarr));
		return out;
	}*/
	
	private static boolean isEqualSetOfIntArray(final Collection<int[]> s1, final Collection<int[]> s2) {
        if (s1 == s2)
            return true;
        if (s1 == null || s2 == null || s1.size() != s2.size())
            return false;
        // careful, this is a O(n^2) check
        top: for (int[] a1 : s1) {
        	for (int[] a2 : s2) {
        		if (Arrays.equals(a1, a2))
        			continue top;
        	}
        	return false; // no match to a1 inside s2
        }
        return true;
    }

	/**
	 * Test method for {@link qclib.util.QuantumUtil#indexGet(org.apache.commons.math3.linear.FieldVector, int[], org.apache.commons.math3.linear.FieldVector)}.
	 * Test method for {@link qclib.util.QuantumUtil#indexSet(org.apache.commons.math3.linear.FieldVector, int[], org.apache.commons.math3.linear.FieldVector)}.
	 * Test method for {@link qclib.util.QuantumUtil#indexMultiplyIn(org.apache.commons.math3.linear.FieldVector, int[], org.apache.commons.math3.linear.FieldVector)}.
	 */
	@Test
	public final void testIndexMethods() {
		FieldVector<Complex> v1 = QuantumUtil.buildVector(0b000, 0b001, 0b010, 0b011, 0b100, 0b101, 0b110, 0b111);
		FieldVector<Complex> v1expected = v1.copy();
		FieldVector<Complex> v2 = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 8);
		FieldVector<Complex> v2expected = QuantumUtil.buildVector(0b000, 0b100, 0b001, 0b101, 0b010, 0b110, 0b011, 0b111);
		int[] targetbits = new int[] {2, 0, 1};
		// v3 represents the modified v2 vector after an Operation
		FieldVector<Complex> v3 = QuantumUtil.buildVector(10, 14, 11, 15, 12, 16, 13, 17);
		FieldVector<Complex> v1expectedAfterSet = QuantumUtil.buildVector(10, 11, 12, 13, 14, 15, 16, 17);
		
		Set<int[]> indset = QuantumUtil.translateIndices(3, targetbits);
		assertTrue( indset.size() == 1 );
		int[] indices = indset.iterator().next();
		assertArrayEquals(new int[] {0,4,1,5,2,6,3,7}, indices);
		
		QuantumUtil.indexGet(v1, indices, v2); 
		assertTrue( v1.equals(v1expected) );
		assertTrue( v2.equals(v2expected) );
		
		QuantumUtil.indexSet(v1, indices, v3);
		assertTrue( v1.equals(v1expectedAfterSet) );
		
		// test #2 -------------------------------------------------------
		
		v1 = QuantumUtil.buildVector(0b000, 0b001, 0b010, 0b011, 0b100, 0b101, 0b110, 0b111);
		v1expected = v1.copy();
		v2 = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 4);
		targetbits = new int[] {1, 2};
		FieldVector<Complex> v4 = v1.copy();
		
		indset = QuantumUtil.translateIndices(3, targetbits);
		assertTrue( indset.size() == 2 );
		
		Set<int[]> expectedIndices = new HashSet<int[]>(Arrays.asList(
				new int[] {1,3,5,7},
				new int[] {0,2,4,6}
		));
		assertTrue(isEqualSetOfIntArray(expectedIndices, indset));
		
		// maps the vectors expected as a result of indexSet to the vectors after a "pretend" Operation
		Map<FieldVector<Complex>,FieldVector<Complex>> expectedMap = new HashMap<FieldVector<Complex>,FieldVector<Complex>>(2);
		expectedMap.put(QuantumUtil.buildVector(0b000, 0b010, 0b100, 0b110), QuantumUtil.buildVector(10, 12, 14, 16));
		expectedMap.put(QuantumUtil.buildVector(0b001, 0b011, 0b101, 0b111), QuantumUtil.buildVector(11, 13, 15, 17));
		
		for (int[] ind : indset) {
			QuantumUtil.indexGet(v1, ind, v2);
			assertTrue( expectedMap.containsKey(v2) );
			
			FieldVector<Complex> v2replacement = expectedMap.get(v2);
			QuantumUtil.indexSet(v1, ind, v2replacement);
			
			// addendum for testIndexMultiplyIn
			QuantumUtil.indexMultiplyIn(v4, ind, v2replacement);
		}
		
		v1expectedAfterSet = QuantumUtil.buildVector(10, 11, 12, 13, 14, 15, 16, 17);
		assertTrue( v1.equals(v1expectedAfterSet) ); 
		
		FieldVector<Complex> v4expectedAfterMultiply = QuantumUtil.buildVector(0*10, 1*11, 2*12, 3*13, 4*14, 5*15, 6*16, 7*17);
		assertTrue( v4.equals(v4expectedAfterMultiply) ); 
	}

	/**
	 * Test method for {@link qclib.Operator#applyTo(qclib.Operator, int, org.apache.commons.math3.linear.FieldVector, int[])}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public final void testDoOp() {
		FieldVector<Complex> v1, v2, v3;
		FieldVector<Complex> v1e, v2e, v3e;
		v1 = QuantumUtil.buildVector(0, 1, 2, 3, 4, 5, 6, 7);
		v1e =QuantumUtil.buildVector(10, 11, 12, 13, 14, 15, 16, 17); // map +10 to all 3 qubits
		
		v2 = QuantumUtil.buildVector(0b000, 0b001, 0b010, 0b011, 0b100, 0b101, 0b110, 0b111);
		v2e =QuantumUtil.buildVector(0b000, 0b001, 0b010, 0b011, -0b100, -0b101, -0b110, -0b111); // Z on bit 2
		// X on bit 2 would give:    0b100, 0b101, 0b110, 0b111, 0b000, 0b001, 0b010, 0b011
		
		v3 = QuantumUtil.buildVector(0b000, 0b001, 0b010, 0b011, 0b100, 0b101, 0b110, 0b111);
		v3e= QuantumUtil.buildVector(0b000, 0b101, 0b010, 0b111, 0b100, 0b001, 0b110, 0b011); // CNOT: control bit 0, target bit 2 -- make target {2,0} b/c second bit is control bit
		
		Operator o1, o2, o3;
		o1 = new Operator(3) {
			@Override
			public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
				return invec.mapAdd(new Complex(10));
			}
		};
		o2 = new Z();
		o3 = new CNOT();
		
		FieldVector<Complex>[] v = new FieldVector[] {v1, v2, v3};
		FieldVector<Complex>[] ve = new FieldVector[] {v1e, v2e, v3e};
		Operator[] o = new Operator[] {o1,o2,o3};
		int[] dvll = new int[] {3,3,3}; // all are operating on a 3-qubit vector
		int[][] tbs = new int[][] {
				new int[] {2,0,1}, // order doesn't matter for first Operation
				new int[] {2},
				new int[] {2,0}
		};
		
		for (int t=0; t<v.length; t++) {
			o[t].applyTo(dvll[t], v[t], tbs[t]);
			assertTrue("testcase["+t+"]:\nresult ="+QuantumUtil.printVector(v[t])+"\nexpected="+QuantumUtil.printVector(ve[t]),
					QuantumUtil.isApproxEqualVector(v[t], ve[t]) );
		}
	}

}
