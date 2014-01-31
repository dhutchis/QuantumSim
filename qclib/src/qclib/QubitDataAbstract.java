package qclib;

import org.apache.commons.math3.complex.Complex;

abstract class QubitDataAbstract {
	public final int numbits;
	/** used to reference all the bits in order (so that we don't have to reference this many times */
	private final int[] allbits_arr;
	
	public QubitDataAbstract(int numbits) {
		this.numbits = numbits;
		this.allbits_arr = new int[numbits];
		for (int i=0; i<numbits; i++)
			allbits_arr[i] = i;
	}
	
	/**
	 * Returns the Complex amplitudes of each bit provided as an argument, respecting order.
	 * If no bits are provided, returns all the bits in order.
	 */
	public final Complex[] getAmp(int... bits) {
		if (bits == null || bits.length == 0)
			bits = allbits_arr;
		for (int bit : bits)
			if (bit < 0 | bit >= numbits)
				throw new IllegalArgumentException("numbits="+numbits+", argument bit out of range:"+bit);
		return customGetAmp(bits);
	}
	
	/** Subclass implements to return the amplitudes */
	protected abstract Complex[] customGetAmp(int... bits);
	
	/**
	 * Set all the bits described by this qubit (shortcut method)
	 */
	public final void setAmp(Complex... amps) {
		setAmp(allbits_arr, amps);
	}
	
	/**
	 * Sets the qubits described by this object. 
	 * Verifies the bits have unit magnitude (proper probability amplitudes).
	 */
	public final void setAmp(int[] bits, Complex[] amps) {
		if (bits == null || bits.length == 0) { // change all the bits
			bits = allbits_arr;
		}
		if (amps == null || amps.length != bits.length)
			throw new IllegalArgumentException("length of target bits ("+bits.length+") != length of amplitudes ("+amps.length+")");
		for (int bit : bits)
			if (bit < 0 | bit >= numbits)
				throw new IllegalArgumentException("numbits="+numbits+", argument bit out of range:"+bit);
		
		customSetAmp(bits,amps);
		checkUnit();
	}

	/** Subclass implements to set the amplitudes of the target bits. */
	protected abstract void customSetAmp(int[] bits, Complex[] amps);

	
	/**
	 * Ensures unit magnitude for sums of squares.
	 */
	private void checkUnit() {
		//TODO: turn off for performance, along with all the above argument error checking
		Complex[] amps = getAmp();
		double sumSquares = 0.0;
		for (Complex c : amps)
			sumSquares += c.abs();
		if (!QuantumUtil.isApproxZero(sumSquares - 1.0))
			throw new IllegalStateException("Qubit data does not have proper amplitudes (squares of amps sum to "+sumSquares+")");
	}
	
	/**
	 * Move the data here somewhere else.  Zeros the data here to keep with the no-clonging theorem.
	 * Note that zeroed data is deliberately invalid since the sum of squares of amplitudes is 0 not 1. 
	 */
	public void moveDataTo(QubitDataAbstract qda2) {
		if (qda2 == null || qda2.numbits != this.numbits)
			throw new IllegalArgumentException("invalid qda2 or bits mismatch");
		qda2.setAmp(this.getAmp());
		Complex[] newValsAfterMove = new Complex[numbits]; // all zero
		this.setAmp(newValsAfterMove);
	}
	
	/**
	 * Returns the number of nonzero amplitudes. Subclasses may override this with a more efficient implementation.
	 */
	public int getNnz() {
		Complex[] vals = this.getAmp();
		int nnz = 0;
		for (Complex val : vals)
			if (!QuantumUtil.isApproxZero(val.getReal()) || !QuantumUtil.isApproxZero(val.getImaginary()))
				nnz++;
		return nnz;
	}
	
}
