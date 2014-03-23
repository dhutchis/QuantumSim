package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;

import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;

public class TestCase {
	
	private QubitRegister qr;
	private int arity;
	public int[] solutions;
	
	private static boolean intArrayContains(int currentState, int[] myArray) {
	    boolean found = false;

	    for (int i = 0; !found && (i < myArray.length); i++) {
	        found = (myArray[i] == currentState);
	    }

	    return found;
	}
	
	/**
	 * @throws InterruptedException 
	 * @throws Exception 
	 */
	public void doGrover(final int arity) throws IllegalArgumentException, InterruptedException {
		this.arity = arity;
		
		this.qr = new QubitRegister(arity+1);
		
		//Build the quantum register with first bit |1> and then 'arity' bits |0>
		this.qr.setAmps( QuantumUtil.buildVector(0,1), 0);
		for(int i=1;i<this.qr.getNumqubits();i++){
			this.qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		
		//Apply H gate to every qubit
		for(int i=0;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
		
		//Uncommenting line below solves the problem
		//this.qr.couple(QuantumUtil.makeConsecutiveIntArray(0, this.arity+1));
		
		//Two variables required for visualisation
		double xc = 0;
		double yc = 0;
		
		//It seems to me that the error is occuring in the getAmps method
		FieldVector<Complex> temp1 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, this.arity+1));
		
		System.out.println(this.qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, this.arity+1)));
		
		for(int i=0;i<(1<<this.arity+1);i=i+2){
			if(intArrayContains((int)Math.floor(i/2), this.solutions)){
				yc += temp1.getEntry(i).getReal();
			} else {
				xc += temp1.getEntry(i).getReal();
			}
		}
		
		//The xc component varies - it's either -0.25 or 1.75
		//If 0 is displayed first by the printBits method then xc = 1.75
		//If 0 is displayed on any other position then xc = -0.25
		System.out.println(xc + " " + yc);
	}
	
	public static void main(String[] args) throws InterruptedException {
		TestCase d = new TestCase();
		
		int[] solutions = new int[1];
		solutions[0] = 0;
		
		d.solutions =solutions;
		
		d.doGrover(3);
	}
}
