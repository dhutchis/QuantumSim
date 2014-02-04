/**
 * 
 */
package qclib.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

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
	
	private Set<Integer[]> intToIntegerArraySet(Set<int[]> in) {
		Set<Integer[]> out = new HashSet<Integer[]>(in.size());
		for (int[] iarr : in)
			out.add(QuantumUtil.intToIntegerArray(iarr));
		return out;
	}
	
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
	 */
	@Test
	public final void testIndexGet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link qclib.util.QuantumUtil#indexSet(org.apache.commons.math3.linear.FieldVector, int[], org.apache.commons.math3.linear.FieldVector)}.
	 */
	@Test
	public final void testIndexSet() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link qclib.util.QuantumUtil#indexMultiplyIn(org.apache.commons.math3.linear.FieldVector, int[], org.apache.commons.math3.linear.FieldVector)}.
	 */
	@Test
	public final void testIndexMultiplyIn() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link qclib.util.QuantumUtil#doOp(qclib.Operator, int, org.apache.commons.math3.linear.FieldVector, int[])}.
	 */
	@Test
	public final void testDoOp() {
		fail("Not yet implemented"); // TODO
	}

}
