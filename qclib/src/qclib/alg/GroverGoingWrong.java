package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;

public class GroverGoingWrong {
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
				outvec.setEntry( x,  invec.getEntry(x).multiply(Math.pow(-1, (x==2) ? 1 : 0)) );
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
		
		//Apply H gate to every qubit
		for(int i=0;i<qr.getNumqubits();i++){qr.doOp(new H(), i);}
		
		//Perform Grover iterations
		System.out.print("\nGrover iteration: "+qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
			
		SpecialF search = new SpecialF(funct, arity);
		
		//QuantumUtil.makeConsecutiveIntArray(0, arity);
		
		qr.doOp(search, QuantumUtil.makeConsecutiveIntArray(0, arity));
		
		/**
		 * Output should be all the states being in the state ( 0,25 , 0    i)
		 * apart from |0010>=(-0,25 ,-0    i)
		 * However, about 1 in 20 runs of the program gives:
		 * |0010>=( 0,25 , 0    i)
		 * |1000>=(-0,25 ,-0    i)
		 */
		
		System.out.print("\nAfter setting solution to negative "+qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		return 1;
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
		GroverGoingWrong d = new GroverGoingWrong();
		long result = d.doGrover(4, new Find2());
	}
}
