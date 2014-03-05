package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.CNOT;
import qclib.op.H;
import qclib.util.QuantumUtil;

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
	public boolean doDeutschJozsa(int arity, FunctionDeutsch funct) {
		assert arity > 1;

		QubitRegister qr = new QubitRegister(arity);
		//Build the quantum register with the first bit in state |1> and the rest in state |0>
		qr.setAmps( QuantumUtil.buildVector(0,1) , 0);
		for(int i=1;i<qr.getNumqubits();i++){
			qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		System.out.print("v0: ");
		System.out.println(QuantumUtil.printVector(qr.getAmps(0,1)));
		
		//Apply H gate to every qubit
		for(int i=0;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		System.out.print("v1: ");
		System.out.println(QuantumUtil.printVector(qr.getAmps(0,1)));
		
		SpecialF special = new SpecialF(funct, arity);
		
		//TODO generalise to
		//qr.doOp(special, [all qubits from 0 to qr.getNumqubits()-1] );

		//Apply function
		qr.doOp(special, 0, 1);
		System.out.print("v2: ");
		System.out.println(QuantumUtil.printVector(qr.getAmps(0,1)));
		
		//Apply H gate to all qubits but first one
		for(int i=1;i<qr.getNumqubits();i++){
			qr.doOp(new H(), i);
		}
		System.out.print("v3: ");
		System.out.println(QuantumUtil.printVector(qr.getAmps(0,1)));
		
		return qr.measure(1);
	}
	
	public static void main(String[] args) {
		Deutsch d = new Deutsch();
		int arity = 2;
		boolean balanced = d.doDeutschJozsa(arity, new FunctionDeutsch(){
			public boolean apply(int argument){
				if(argument == 0){
					return false;
				}
				return true;
			}
		});
		
		System.out.println("Balanced: "+balanced);
	}
}
