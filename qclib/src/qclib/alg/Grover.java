package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;

public class Grover {
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
			
			for (int x = 0; x < 1<<this.getArity(); x++) {
				// only negate 2nd entry (starting from index 0)
				if (x == 2)
					outvec.setEntry(x, invec.getEntry(x).negate());
				else
					outvec.setEntry(x, invec.getEntry(x));
			}
			
			return outvec;
		}
		
	}
	
	
	/**
	 * Always works.
	 * @param funct
	 * @return true if balanced, false if constant
	 */
	public long doGrover(int arity, FunctionDeutsch funct) {
		assert arity > 1;

		QubitRegister qr = new QubitRegister(arity);
		
		//Build the quantum register with all the bits |0>
		//qr.setAmps( QuantumUtil.buildVector(0,1), 0);
		for(int i=0;i<qr.getNumqubits();i++){
			qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		System.out.print("v0: "+ qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		//Apply H gate to every qubit
		for(int i=0;i<qr.getNumqubits();i++){qr.doOp(new H(), i);}
		System.out.print("\nv1: "+ qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		//Perform Grover iterations
		for(int j=0;j<1;j++){//<Math.ceil(Math.PI*Math.sqrt(1 << arity)/4);j++){
			System.out.print("\nGrover iteration: "+j+"\n"+qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
			
			SpecialF search = new SpecialF(funct, arity);
					
			qr.doOp(search, QuantumUtil.makeConsecutiveIntArray(0, arity));
			
			System.out.print("\nAfter setting solution to negative "+qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
			for(int i=0;i<qr.getNumqubits();i++){qr.doOp(new H(), i);}
			
			FieldVector<Complex> temp2 = qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, arity));			
			//System.out.print("\n"+QuantumUtil.printVector(qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, arity))));
			for(int i=0;i<(1<<arity);i++){
				temp2.setEntry(i, temp2.getEntry(i).negate());
			}
			
			qr.setAmps(temp2, QuantumUtil.makeConsecutiveIntArray(0, arity));
			
			for(int i=0;i<qr.getNumqubits();i++){qr.doOp(new H(), i);}
		}
		//System.out.print("\nv2: "+qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		//Measurement
		long result = 0;
		for(int i=0;i<arity;i++){
			if(qr.measure(i)){
				result += (1 << i);
			}
		}
		return result;
	}
	
	/** constant test function for any number of qubits */
	private static class Find2 implements FunctionDeutsch {

		
		@Override
		public boolean apply(int argument){
			if(argument==2){
				return true;
			}
			return false;
		}
	}
	
	public static void main(String[] args) {
		Grover d = new Grover();
		long result = d.doGrover(4, new Find2());
		//System.out.println("Result: " + result);
	}
}
