package qclib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.Pair;

import qclib.util.QuantumUtil;

public abstract class Operator {
	/** How many qubits the operator operates on. */
	private int arity;
	
	public Operator(int arity) {
		if (arity <= 0)
			throw new IllegalArgumentException("bad arity: "+arity);
		this.arity = arity;
	}

	public int getArity() { return arity; }
	
	/**
	 * Given a vector with qubit amplitudes in standard order, 
	 *   return a new vector with the results of the operator application.
	 * Ex. for 2 qubits, vector is in order of {|00>, |01>, |10>, |11>} 
	 * @param invec vector of length 2^arity
	 * @return vector of length 2^arity
	 */
	public abstract FieldVector<Complex> apply(FieldVector<Complex> invec);
	// should I do error checking- For vector size?
	
	/**
	 * Returns a new Operator that applies op2, then applies this.
	 * @param op2 The Operator to apply FIRST.
	 * @return New Operator.
	 */
	public Operator curryAfter(final Operator op2) {
		if (op2 == null || op2.getArity() != arity)
			throw new IllegalArgumentException("bad operator argument does not match arity: this="+this+", op2="+op2);
		
		return new Operator(arity) {
			@Override
			public FieldVector<Complex> apply(FieldVector<Complex> invec) {
				return this.apply(op2.apply(invec));
			}
			
		};
	}
	
	/**
	 * Creates a log-k arity operator, where k is the number of unique values (bits) in opmap.
	 * 		These values should cover [0, 1, ..., k-1] uniquely.
	 * Performs each operation in turn.  Since they occur on independent bits, the operations can be done in any order.
	 * @param opmap
	 * @return THe log-k arity combined Operator.
	 */
	public static Operator combineIndependentOps(final Map<Operator, Integer[]> opmap) {
		// error-checking: integers should be unique, cover 0..k-1
		// turn off for performance
		if (opmap == null || opmap.size() == 0)
			throw new IllegalArgumentException("bad map: "+opmap);
		SortedSet<Integer> intset = new TreeSet<Integer>(); // sorts the ints
		for(Integer[] intarr : opmap.values()) {
			for (Integer i : intarr) {
				if (intset.contains(i))
					throw new IllegalArgumentException("duplicate bit "+i+" specified in map: "+opmap);
				intset.add(i);
			}
		}		
		if (intset.first() != 0)
			throw new IllegalArgumentException("smallest bit specified in map is not 0: "+opmap);
		int k = intset.last()+1;
		if (!ArithmeticUtils.isPowerOfTwo(k))
			throw new IllegalArgumentException("largest bit specified in map is not a power of 2: "+opmap);
		
		return new Operator(k) {

			@Override
			public FieldVector<Complex> apply(FieldVector<Complex> invec) {
				for (Entry<Operator,Integer[]> entry : opmap.entrySet()) {
					Operator op = entry.getKey();
					int[] intarg = QuantumUtil.integerToIntArray(entry.getValue());
					//invec = QubitContainer.doOpStatic(op, invec, intarg);
					// TODO!!!
				}

				
				return null;
			}
			
		};
	}
	
	/*public Operator combineIndependentOps(int[] mybits, Pair<Integer[],Operator>... otherops) {
		// verify valid arguments
		if (mybits == null || mybits.length != numbits)
			throw new IllegalArgumentException("bad number of bits specified; must match arity "+numbits+" but actually given"+mybits.length);
		//if (otherops == null)
			//return this; // no other operators to tensor with - actually might swap argument bits
		// build a map of integers to 
		for (Pair<Integer[],Operator> pair : otherops) {
			Integer[] otherbits = pair.getFirst();
			Operator otherop = pair.getSecond();
			// need all bits to be unique
			if (otherbits.length != otherop.arity)
				throw new IllegalArgumentException("bad number of bits specified for Operator "+otherop+"; must match arity "+otherop+" but actually given"+otherbits.length);
			
		}
	}*/
	
	// idea for method: public Operator tensorUp(int[] mybits, Operator[] otherops, int[][] otherbits)
	// combine two 1-arg Operators into a 2-arg Operator
	// if bit not handled, then assume identity Operation on that bit
}
