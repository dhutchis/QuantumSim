package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

public class Deutsch {

	
	/**
	 * on arity qubits total. --- x is all but last qubit, y is last qubit
	 * 
	 */
	static class SpecialF extends Operator {

		private FunctionDeutsch funct;
		
		public SpecialF(FunctionDeutsch funct, int arity) {
			super(arity);
			assert arity >= 2;
			this.funct = funct;
		}

		@Override
		protected FieldVector<Complex> myApply(FieldVector<Complex> invec) {
			FieldVector<Complex> outvec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), invec.getDimension());
			outvec.set(Complex.ZERO);
			
			for (int x = 0; x < 1<<(this.getArity()-1); x++)
				for (int y=0; y <= 1; y++) {
					
					int idxin = (x << 1) | y;
					int idxout = (x << 1) | (y ^ (funct.apply(x) ? 1 : 0));
					
					outvec.setEntry( idxout,  outvec.getEntry(idxout).add(invec.getEntry(idxin)) );
					
				}
			
			return outvec;
		}
		
	}
	
	
	/**
	 * Always works.
	 * @param funct
	 * @return true if balanced, false if constant
	 */
	public boolean doDeutsch(FunctionDeutsch funct) {
		return false;
	}
	
	public static void main(String[] args) {
		// run Deutsch on something
	}
	
	// TODO: test Deutsch
	// TODO first: test SpecialF
	
}
