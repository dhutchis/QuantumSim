package qclib;

import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.util.Pair;

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
	
	
	
	// idea for method: public Operator tensorUp(int[] mybits, Operator[] otherops, int[][] otherbits)
	// combine two 1-arg Operators into a 2-arg Operator
	// if bit not handled, then assume identity Operation on that bit
}
