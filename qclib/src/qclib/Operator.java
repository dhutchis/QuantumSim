package qclib;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.util.BitSetUtil;
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
	 * *Performs error checking before calling a subclass's myApply method.*
	 * Given a vector with qubit amplitudes in standard order, 
	 *   return a new vector with the results of the operator application.
	 * Ex. for 2 qubits, vector is in order of {|00>, |01>, |10>, |11>} 
	 * @param invec vector of length 2^arity
	 * @return vector of length 2^arity
	 */
	public final FieldVector<Complex> apply(FieldVector<Complex> invec) {
		if (invec == null)
			throw new IllegalArgumentException("no null's please");
		if (invec.getDimension() != 1<<arity)
			throw new IllegalArgumentException("bad operator argument does not match arity: this="+this.arity+", inevc dimension="+invec.getDimension());
		
		FieldVector<Complex> result = this.apply(invec);
		
		// sanity check for subclasses: result vector has same dimension
		assert (result.getDimension() != 1<<arity) : "bad subclass; returned a vector of different dimension";
		return result;
	}
	
	/**
	 * *Subclasses must override this method. Error checking performed before call.*
	 * Given a vector with qubit amplitudes in standard order, 
	 *   return a new vector with the results of the operator application.
	 * Ex. for 2 qubits, vector is in order of {|00>, |01>, |10>, |11>} 
	 * @param invec vector of length 2^arity
	 * @return vector of length 2^arity
	 */
	protected abstract FieldVector<Complex> myApply(FieldVector<Complex> invec);
	// should I do error checking- For vector size?
	
	/**
	 * Returns a new Operator that applies this, then applies op2.
	 * Use like this: op1.curryBefore(op2).curryBefore(op3)
	 * 		which executes op1, then op2, then op3
	 * @param op2 The Operator to apply SECOND.
	 * @return New Operator. 
	 */
	public Operator curryBefore(final Operator op2) {
		if (op2 == null || op2.getArity() != arity)
			throw new IllegalArgumentException("bad operator argument does not match arity: this="+this+", op2="+op2);
		
		final Operator outside = this;
		return new Operator(arity) {
			@Override
			public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
				return op2.myApply(outside.myApply(invec));
			}
			
		};
	}
	
	/**
	 * Returns a new Operator that calls this one, passing newbits[i] to Operator bit i.
	 * Equivalent to this.extend(this.getArity(), newbits);
	 * @param newbits The bits 0, 1, ..., getArity()-1 in some new order 
	 * @return new Operator
	 */
	public Operator swapBits(int... newbits) {
		return this.extend(arity, newbits);
		/*checkSetUniquelyK(true, arity, newbits);
		
		Set<int[]> transet = QuantumUtil.translateIndices(arity, newbits);
		assert transet.size() == 1;
		final int[] indices = transet.iterator().next();
		final Operator outside = this;
		
		return new Operator(arity) {
			@Override
			public FieldVector<Complex> apply(FieldVector<Complex> invec) {
				FieldVector<Complex> remappedVec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<arity);
				QuantumUtil.indexGet(invec, indices, remappedVec);
				remappedVec = outside.apply(remappedVec);
				QuantumUtil.indexSet(invec, indices, remappedVec);
				return invec;
			}
		};*/
		
	}
	
	
	
	private static void checkSetUniquelyK(boolean mustCoverAll, int k, int... s) {
		checkSetUniquelyK(mustCoverAll, k, Collections.singleton(s));
	}
	
	/*
	private static <T> Iterable<T> flattenCollection(final Iterable<? extends Iterable<T>> iterableHigh) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				final Iterator<? extends Iterable<T>> iterHigh = iterableHigh.iterator();
				
				return new Iterator<T>() {
					private Iterable<T> iterableLowNext;
					private Iterator<T> iterLow, iterLowNext;
					private boolean iterLowNextReady = false;

					@Override
					public boolean hasNext() {
						if (iterLow == null || !iterLow.hasNext()) {
							if (!iterLowNextReady) {
								// get next iterLow from iterHigh, unless we run out
								do {
									if (!iterHigh.hasNext())
										return false;
									iterableLowNext = iterHigh.next();
									iterLowNext = iterableLowNext.iterator();
								} while (!iterLowNext.hasNext());
								iterLowNextReady = true;
							}
							return true;
						}
						return iterLow.hasNext();	
					}

					@Override
					public T next() {
						if (!hasNext())
							throw new NoSuchElementException();
						if (iterLow != null && iterLow.hasNext())
							return iterLow.next();
						// must be the case that iterHigh has another iterator ready
						assert iterLowNextReady;
						// engage it!
						iterLow = iterLowNext;
						iterLowNextReady = false;
						return iterLow.next();
					}

					@Override
					public void remove() {
						if (iterLow == null)
							throw new IllegalStateException("call next() before remove()");
						iterLow.remove();
					}
					
				};
			}
			
		};
	}*/
	
	/** Checks that each int in {0,1,...,k-1} is uniquely contained in the set of int[]s. Throws an exception if not. */
	private static void checkSetUniquelyK(boolean mustCoverAll, int k, Iterable<int[]> s) {
		if (k < 0 || s == null)
			throw new IllegalArgumentException("bad arguments");
		Set<Integer> kset = QuantumUtil.intArrayToSet(QuantumUtil.makeConsecutiveIntArray(0, k));
		for (int[] arr : s)
			for (int i : arr) {
				if (i < 0 || i >= k || !kset.remove(i))
					throw new RuntimeException(i+" is a duplicate or not in the range {0,1,...,"+(k-1)+"}");
			}
		if (mustCoverAll)
			if (kset.size() > 0)
				throw new RuntimeException(BitSetUtil.printIntArray((Integer[])kset.toArray())+" are not covered");
	}
	
	/**
	 * Extend this operator on n-qubits to an m-qubit operator, m >= n.
	 * @param extendedArity m
	 * @param targetbits which bits among {0,1,...,m-1} to pass to bits {0,1,...,n-1} of op.
	 */
	public Operator extend(final int extendedArity, final int... targetbits) {
		checkSetUniquelyK(false, extendedArity, targetbits);
		if (targetbits.length != arity)
			throw new IllegalArgumentException("targetbits should have the same length as arity");
		
		// TODO: test this class
		// This method generalizes swapBits( ).  Combine the two.
		
		final Set<int[]> transet = QuantumUtil.translateIndices(extendedArity, targetbits);
		final Operator outside = this;
		
		return new Operator(extendedArity) {
			@Override
			public FieldVector<Complex> myApply(FieldVector<Complex> invec) {
				// TODO check this: for loop through the appropriate indices
				FieldVector<Complex> remappedVec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<extendedArity);
				for (int[] indices : transet) {
					QuantumUtil.indexGet(invec, indices, remappedVec);
					remappedVec = outside.apply(remappedVec);
					QuantumUtil.indexSet(invec, indices, remappedVec);
				}
				return invec;
			}
		};
	}

	/**
	 * Apples this Operator to a given datavec and stores the result in datavec.
	 * Length of datavec is 2^datavecloglength
	 * @param op
	 * @param data
	 * @param targetbits
	 */
	public void applyTo(int datavecloglength, FieldVector<Complex> datavec, int... targetbits) {
		if (targetbits == null || datavec == null || targetbits.length > datavecloglength)
			throw new IllegalArgumentException("bad arguments to QuantumUtil.doOp");
		if (1<<datavecloglength != datavec.getDimension())
			System.err.println("Warning: datavecloglength="+datavecloglength+", datavec.getDimension()="+datavec.getDimension());
		
		// vector to hold the amplitudes to pass to the operator
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),1<<targetbits.length);
		// map indices in this.data to indices in vec, in order specified by targetbits
		Set<int[]> indexset = QuantumUtil.translateIndices(datavecloglength, targetbits);
		// for each set of indices indexing into this.data
		for (int[] indices : indexset) {
			// get the amplitudes from this.data into vec, do the operator on vec to get a new vec, and set the new amplitudes from vec into this.data 
			QuantumUtil.indexGet(datavec, indices, vec);
			vec = this.myApply(vec);
			QuantumUtil.indexSet(datavec, indices, vec);
		}
	}
	
	/**
	 * Creates a log-k arity operator, where k is the number of unique values (bits) in opmap.
	 * 		These values should be unique within [0, 1, ..., k-1].
	 * *Note: allowed to not cover some of the bits.  Assume the identity operator on those bits not specified.
	 * **Note: allowed to use the same Operator object more than once.  That's why the value of the map is a List<int[]> instead of just int[]
	 * 	   ex: To use the Z operator on both bits 1 and 0, make a Map( Z -> [ {1}, {0} ] )
	 * Performs each operation in turn.  Since they occur on independent bits, the operations can be done in any order.
	 * 
	 * TODO: HEY -- IT SEEMS TH OPERATIONS MAY NOT COMMUTE / ARE NOT INDEPENDENT.  How should this method work / be used??
	 * 
	 * @param opmap
	 * @return THe log-k arity combined Operator.
	 */
	/*public static Operator combineIndependentOps(final int logk, final Map<Operator, List<int[]>> opmap) {
		// error-checking: integers should be unique, cover 0..k-1
		// turn off for performance
		if (opmap == null || opmap.size() == 0 || logk <= 0)
			throw new IllegalArgumentException("bad map: "+opmap+" or logk="+logk);
		checkSetUniquelyK(false, logk, flattenCollection(opmap.values()));
		
		return new Operator(logk) {

			@Override
			public FieldVector<Complex> apply(FieldVector<Complex> invec) {
				// apply each operator in the map; order doesn't matter
				// (no operation performed on bits not in map)
				for (Entry<Operator,List<int[]>> entry : opmap.entrySet()) {
					Operator op = entry.getKey();
					// use op for each int[] in list; if >1, it means op is being applied multiple times, in order
					for (int[] intarg : entry.getValue()) 
						QuantumUtil.doOp(op, logk, invec, intarg); // modifies invec
				}				
				return invec;
			}
			
		};
	}*/
	
}
