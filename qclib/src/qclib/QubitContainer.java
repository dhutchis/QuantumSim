package qclib;

import java.text.DecimalFormat;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.linear.SparseFieldVector;

import qclib.util.QuantumUtil;

/**
 * 
 *
 */
@SuppressWarnings("deprecation")
public class QubitContainer {
	private int numbits;
	/** Either a dense or sparse vector of data.
	 *  Ex. 2 qubits => {|00>, |01>, |10>, |11>} */
	private FieldVector<Complex> data;
	
	/** Create a dense container. */
	public QubitContainer(int numbits) { 
		this(numbits, false);
	}
	
	/** Qubit container initially contains 1 in the |00...0> amplitude and 0 everywhere else. */
	public QubitContainer(int numbits, boolean isSparse) {
		if (numbits <= 0)
			throw new IllegalArgumentException("bad number of qubits: "+numbits);
		this.numbits = numbits;
		if (isSparse) {
			data = new SparseFieldVector<Complex>(ComplexField.getInstance(), 1<<numbits);
		} else {
			data = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<numbits);
		}
		data.setEntry(0, Complex.ONE);
	}
	
	public int getNumbits() { return numbits; }
	
	
	private static final DecimalFormat realFormat = new DecimalFormat(" 0.###;-0.###");
	private String padToLength(String s, int len) {
		while (s.length() < len)
			s += ' ';
		return s;
	}
	
	/** {|0>=_, |1>=_, |2>=_, |3>=_} */
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder("{ ");
		for (int i=0; i < (1<<numbits); i++) {
			Complex c = data.getEntry(i);
			sb.append('|');
			// print leading zeros on the index from toBinaryString
			String idxString = Integer.toBinaryString(i);
			for (int j=numbits - idxString.length(); j > 0; j--)
				sb.append('0');
			
			sb.append(idxString).append(">=(")
				.append(padToLength(realFormat.format(c.getReal()), 6)).append(',')
				.append(padToLength(realFormat.format(c.getImaginary()), 6)).append("i)");
			
			if (i == (1<<numbits)-1)
				sb.append(" }\n");
			else if (i % 4 == 3)			// put long vector on multiple lines
				sb.append("\n   ");
			else 
				sb.append(", ");
		}
		return sb.toString();
	}
	
	/**
	 * Permute the bit ordering.  
	 * Ex.: reorderBits(2,0,1) will make the previous bit 2 the new bit 0, previous bit 0 the new bit 1, previous bit 1 the new bit 2
	 * @param neworder
	 */
	public void reorderBits(int... neworder) {
		Operator.checkSetUniquelyK(true, this.getNumbits(), neworder);
		
		Set<int[]> transet = QuantumUtil.translateIndices(this.getNumbits(), neworder);
		assert transet.size() == 1;
		final int[] indices = transet.iterator().next();
		
		FieldVector<Complex> remappedVec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<this.getNumbits());
		QuantumUtil.indexGet(data, indices, remappedVec);
		data = remappedVec; // replace data with same data in new order
	}
	
	/** Returns a copy of the data vector. */
	public FieldVector<Complex> getAmps() {
		return data.copy(); 
	}
	
	/** 
	 * Sets amplitudes and returns itself.
	 * @param amps Complex Vector of length 2^numbits
	 * @return this
	 */
	public QubitContainer setAmps(FieldVector<Complex> amps) {
		if (amps == null || amps.getDimension() > (1<<numbits))
			throw new IllegalArgumentException("bad number of amps given: "+amps);
		data = amps.copy();
		//checkUnit(); // for safety
		return this;
	}
	
	/**
	 * Ensures unit magnitude for sums of squares of amplitudes.
	 */
	@SuppressWarnings("unused")
	private void checkUnit() {
		//TODO: turn off for performance, along with all the above argument error checking
		double sumSquares = 0.0;
		for (Complex c : data.toArray())
			sumSquares += c.abs()*c.abs();
		if (!QuantumUtil.isApproxZero(sumSquares - 1.0))
			throw new IllegalStateException("Qubit data does not have proper amplitudes (squares of amps sum to "+sumSquares+")");
	}
	
	
	/*
	 * Apply op to the target bits of invec and return a new (dense) vector, 
	 * 		size = targetbits.length, with the results.
	 * @param op
	 * @param invec
	 * @param targetbits
	 * @return
	 /
	public static FieldVector<Complex> doOpStatic(Operator op, 
			FieldVector<Complex> invec, int... targetbits) {
		if (op == null || targetbits == null || targetbits.length > numbits 
				|| op.getArity() != targetbits.length)
			throw new IllegalArgumentException("bad argument operator targetbits");
		int targetbitmask = 0;
		for (int tb : targetbits)
			targetbitmask |= 1<<tb;
		FieldVector<Complex> outvec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),targetbits.length); 
		doOpFree(op, targetbits, 0, targetbitmask, 0, invec, outvec);
		return null;
	}*/
	
	// TODO
	// translateVectorBigToSmall(
	// translateVectorSmallToBig(
	
	
	
	/**
	 * Perform Operation on the targetbits in the order specified.
	 * Call with either this.doOp(op, new int[] {3, 1});
	 * 				or	this.doOp(op, 3, 1);
	 * @param op
	 * @param targetbits
	 * @return this.  Allows for chaining: QubitContainer qc.doOp(H).doOp(Z).doOp(I)
	 */
	public QubitContainer doOp(Operator op, int... targetbits) {
		if (op == null || targetbits == null || targetbits.length > numbits 
				|| op.getArity() != targetbits.length)
			throw new IllegalArgumentException("bad argument operator targetbits");

		op.applyTo(numbits, this.data, targetbits);
		return this;
	}
	
	
	
	/// ---------------------
	/// MEASUREMENT FUNCTIONS
	/// ---------------------
	
	/**
	 * Measure a single bit. Uses probability according to the amplitudes.
	 * Collapses the container to a state with only the measured bit.
	 * @param targetbit which bit to measure
	 * @return boolean true for |1> and false for |0>
	 */
	public boolean measure(int targetbit) {
		if (targetbit < 0 || targetbit >= numbits)
			throw new IllegalArgumentException("bad target measurement bit: "+targetbit);
		double sumSquaresZero = measureGetSumSquares(targetbit, 0, 0);
		double sumSquaresOne = measureGetSumSquares(targetbit, 0, 1<<targetbit);
		assert QuantumUtil.isApproxZero(sumSquaresZero + sumSquaresOne - 1); // sanity check
		
		// return 0 with probability sumSquaresZero
		// return 1 with probability 1-sumSquaresZero == sumSquaresOne
		boolean result = Math.random() > sumSquaresZero;
		// collapse the state via normalization
		double sumSquaresResult = result ? sumSquaresOne : sumSquaresZero;
		if (!QuantumUtil.isApproxZero(sumSquaresResult)) {
			// avoided division by zero if we don't need to normalize!
			// if measure 0, normalize all the amplitudes with a 0 at targetbit
			//		and set all the amplitudes with a 1 at targetbit 
			measureNormalize(targetbit, 0, result ? 1<<targetbit : 0, Math.sqrt(sumSquaresResult));
			measureSetZero(targetbit, 0, result ? 0 : 1<<targetbit);
		}
		return result;
	}
	
	/** Helper Recursive method through each bit, skipping the targetbit.
	 * Sum the squares of each coefficient, holding targetbit constant and varying all other free bits
	 * @param targetbit the bit to skip (hold constant)
	 * @param bitnum the current bit considered; ranges 0 -> numbits-1
	 * @param bitaddr the current address under construction
	 * @return a probability equal to the sum of the squares of coefficients with targetbit held fixed
	 */
	private double measureGetSumSquares(final int targetbit, int bitnum, int bitaddr) {
		if (bitnum == targetbit)
			return measureGetSumSquares(targetbit, bitnum+1, bitaddr);
		if (bitnum >= numbits)
			return QuantumUtil.square( data.getEntry(bitaddr).abs() );
		double tmp = measureGetSumSquares(targetbit, bitnum+1, bitaddr);
		tmp += measureGetSumSquares(targetbit, bitnum+1, bitaddr | (1<<bitnum));
		return tmp;
	}
	
	/** divide the coefficients of each bit by norm,
	 * holding targetbit constant and varying the other free bits
	 * @param targetbit
	 * @param bitnum
	 * @param bitaddr
	 * @param norm the amount to normalize by
	 */
	private void measureNormalize(final int targetbit, int bitnum, int bitaddr, final double norm) {
		if (bitnum == targetbit)
			measureNormalize(targetbit, bitnum+1, bitaddr, norm);
		else if (bitnum >= numbits)
			data.setEntry(bitaddr, data.getEntry(bitaddr).divide(norm));
		else {
			measureNormalize(targetbit, bitnum+1, bitaddr, norm);
			measureNormalize(targetbit, bitnum+1, bitaddr | (1<<bitnum), norm);
		}
	}
	
	/** Set the coefficients of each bit to zero,
	 * holding targetbit constant and varyign the other free bits */
	private void measureSetZero(final int targetbit, int bitnum, int bitaddr) {
		if (bitnum == targetbit)
			measureSetZero(targetbit, bitnum+1, bitaddr);
		else if (bitnum >= numbits)
			data.setEntry(bitaddr, Complex.ZERO);
		else {
			measureSetZero(targetbit, bitnum+1, bitaddr);
			measureSetZero(targetbit, bitnum+1, bitaddr | (1<<bitnum));
		}
	}
	
}
