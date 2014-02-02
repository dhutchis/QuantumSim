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
	 * 		These values should be unique within [0, 1, ..., k-1].
	 * Note: allowed to not cover some of the bits.  Assume the identity operator on those bits not specified.
	 * Performs each operation in turn.  Since they occur on independent bits, the operations can be done in any order.
	 * @param opmap
	 * @return THe log-k arity combined Operator.
	 */
	public static Operator combineIndependentOps(final int logk, final Map<Operator, int[]> opmap) {
		// error-checking: integers should be unique, cover 0..k-1
		// turn off for performance
		if (opmap == null || opmap.size() == 0 || logk <= 0)
			throw new IllegalArgumentException("bad map: "+opmap+" or logk="+logk);

		Set<Integer> intset = new HashSet<Integer>();
		int k = 1<<logk;
		for(int[] intarr : opmap.values()) {
			for (int i : intarr) {
				if (intset.contains(i))
					throw new IllegalArgumentException("duplicate bit "+i+" specified in map: "+opmap);
				intset.add(i);
				if (i < 0 || i >= logk)
					throw new IllegalArgumentException("bit "+i+" out of range as specified in map: "+opmap);
			}
		}
		
		return new Operator(logk) {

			@Override
			public FieldVector<Complex> apply(FieldVector<Complex> invec) {
				// apply each operator in the map; order doesn't matter
				// (no operation performed on bits not in map)
				for (Entry<Operator,int[]> entry : opmap.entrySet()) {
					Operator op = entry.getKey();
					int[] intarg = entry.getValue();
					
					QuantumUtil.doOp(op, logk, invec, intarg); // modifies invec
				}				
				return invec;
			}
			
		};
	}
	
}
