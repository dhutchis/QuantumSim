package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;

/**
 * Class implementing Deutsch-Jozsa algorithm which is a generalization of Deutsch's algorithm.
 * For arity 2 system it reduces to Deutsch's algorithm.
 * Using just one evaluation of the function it recognises if the function provided is constant or balanced.
 * It is assumed that the function provided is either constant or balanced.
 */
public class DeutschJozsa {
	/**
	 * Algorithm operates on arity qubits in total.
	 * x is all but last qubit, y is last qubit.
	 */
	
	/**
	 * Implementing function provided as an operator changing the last qubit y.
	 */
	static class SpecialF extends Operator {

		private FunctionFilter funct;
		
		public SpecialF(FunctionFilter funct, int arity) {
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
	 * Performs Deutsch-Jozsa algorithm.
	 * For arity 2 system it reduces to Deutsch's algorithm.
	 * Always works.
	 * @param arity - the number of the qubits used for as the arguments for the function
	 * @param funct - function which is assumed to be either constant or balanced
	 * @return true if balanced, false if constant
	 */
	public boolean doDeutschJozsa(int arity, FunctionFilter funct) {
		assert arity > 1;

		QubitRegister qr = new QubitRegister(arity);
		
		//Build the quantum register with the first bit in state |1> and the rest in state |0>
		qr.setAmps( QuantumUtil.buildVector(0,1) , 0);
		for(int i=1;i<qr.getNumqubits();i++){
			qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		
		//Apply H gate to every qubit
		for(int i=0;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		
		SpecialF special = new SpecialF(funct, arity);
		
		//Performs the evaluation of the function
		qr.doOp(special, QuantumUtil.makeConsecutiveIntArray(0, arity) );
		
		//Apply H gate to all qubits but first one
		for(int i=1;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		
		//Measurement
		//"If Alice measures all 0s then the function is constant;
		//otherwise the function is balanced."
		boolean balanced = false;		
		for(int i=1;i<qr.getNumqubits();i++){
			if(qr.measure(i)!=false){
				balanced = true;
				break;
			}
		}
		
		return balanced;
	}
	
	/** constant test function for any number of qubits */
	private static class FunDeutchNC implements FunctionFilter {
		@Override
		public boolean apply(int argument){
			return true;
		}
	}
	
	/** balanced test function for any number of qubits */
	private static class FunDeutchNB implements FunctionFilter {
		@Override
		public boolean apply(int argument){
			return argument % 2 == 0; // returns true for even arguments, false for odd arguments 
		}
	}
	
	public static void main(String[] args) {
		DeutschJozsa d = new DeutschJozsa();
		
		boolean balanced = d.doDeutschJozsa(2, new FunDeutchNC());
		assert !balanced;
		
		balanced = d.doDeutschJozsa(2, new FunDeutchNB());
		assert balanced;
		
		balanced = d.doDeutschJozsa(3, new FunDeutchNC());
		assert !balanced;
		
		balanced = d.doDeutschJozsa(3, new FunDeutchNB());
		assert balanced;
		
		balanced = d.doDeutschJozsa(5, new FunDeutchNC());
		assert !balanced;
		
		balanced = d.doDeutschJozsa(5, new FunDeutchNB());
		assert balanced;
	}
}
