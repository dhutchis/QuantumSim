package qclib;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.FieldVector;

public class MatrixOperator extends Operator {
	
	private FieldMatrix<Complex> matrixop;
	
	public MatrixOperator(int arity, FieldMatrix<Complex> matrixop) {
		super(arity);
		this.matrixop = matrixop;
	}

	@Override
	protected FieldVector<Complex> myApply(FieldVector<Complex> invec) {
		return matrixop.operate(invec);
	}
	
	

	/** 
	 * Efficiency boost when composing matix operators:
	 * 	Do matrix multiplication instead of just chaining an extra function call on the stack.
	 *  This trades some minimal precomputation for better runtime performance.
	 * 
	 * @see qclib.Operator#curryBefore(qclib.Operator)
	 */
	@Override
	public Operator curryBefore(Operator op2) {
		if (op2 instanceof MatrixOperator) {
			// other operator is a MatrixOperator
			// efficiently combine the operators by doing matrix multiplication
			// result is that we don't have to have as many function calls at runtime
			// we can precompute the chained matrix multiplication
			MatrixOperator mop2 = (MatrixOperator)op2;
			return new MatrixOperator(this.getArity(), this.matrixop.preMultiply(mop2.matrixop));
		} else {
			// other operator is not a MatrixOperator
			// just do usual function composition
			return super.curryBefore(op2);			
		}
		
	}

	public static void main(String[] args) {
		

		
		
	}

}
