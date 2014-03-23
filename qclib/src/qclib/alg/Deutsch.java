package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;

public class Deutsch {
	/**
	 * on arity qubits total. --- x is all but last qubit, y is last qubit
	 * 
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
	 * Always works.
	 * @param funct
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
		System.out.print("v0: "+qr.printBits(0,1));
		
		//Apply H gate to every qubit
		for(int i=0;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		System.out.print("\nv1: "+qr.printBits(0,1));
		
		SpecialF special = new SpecialF(funct, arity);
		
		// generalized to arity qubits
		qr.doOp(special, QuantumUtil.makeConsecutiveIntArray(0, arity) );

		//Apply function
		//qr.doOp(special, 0, 1); old version
		System.out.print("\nv2: "+qr.printBits(0,1));
		
		//Apply H gate to all qubits but first one
		for(int i=1;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		System.out.print("\nv3: "+qr.printBits(0,1));
		
		//Measurement
		//"If Alice measures all 0s then the function is constant;
		//otherwise the function is balanced."
		boolean balanced = false;		
		for(int i=1;i<qr.getNumqubits();i++){
			if(qr.measure(i)!=false){
				System.out.print(qr.printBits(0,1));
				balanced = true;
				break;
			}
			System.out.print(qr.printBits(0,1));
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
		Deutsch d = new Deutsch();
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
		
		//TODO test file
		/*if(balanced){
			System.out.println("Balanced");
		} else {
			System.out.println("Constant");
		}*/
	}
}
