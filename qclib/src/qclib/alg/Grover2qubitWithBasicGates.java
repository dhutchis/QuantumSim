package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.CNOT;
import qclib.op.H;
import qclib.op.Z;
import qclib.op.X;
import qclib.op.ComboOps;
import qclib.util.QuantumUtil;
import qclib.util.CartesianRepresentation;

public class Grover2qubitWithBasicGates {
	
	private QubitRegister qr;
	private CartesianRepresentation visualisation;
	private int arity;
	private boolean visualise;
	private int[] solutions;
	private int visualisationDelayTime;
	
	public Grover2qubitWithBasicGates(){
		this.setVisualisation(false);
		this.setVisualisationDelayTime(500);
	}
	
	public Grover2qubitWithBasicGates(boolean visualise){
		this.setVisualisation(visualise);
		this.setVisualisationDelayTime(500);
	}
	
	public Grover2qubitWithBasicGates(int visualisationDelayTime){
		this.setVisualisation(true);
		this.setVisualisationDelayTime(visualisationDelayTime);
	}
	
	public Grover2qubitWithBasicGates(boolean visualise, int visualisationDelayTime){
		this.setVisualisation(visualise);
		this.setVisualisationDelayTime(visualisationDelayTime);
	}
	
	public void setVisualisation(boolean visualise){
		if(visualise){
			this.visualisation = new CartesianRepresentation();
		} else {
			this.visualisation = null;
		}
		this.visualise = visualise;
	}
	
	public boolean getVisualisation(){
		return this.visualise;
	}
	
	public void setVisualisationDelayTime(int visualisationDelayTime){
		this.visualisationDelayTime = visualisationDelayTime;
	}
	
	public int getVisualisationDelayTime(){
		return this.visualisationDelayTime;
	}
	
	public void setSolutions(int[] solutions){
		this.solutions = solutions.clone();
	}
	
	public int[] getSolutions(){
		return this.solutions;
	}
	
	private static boolean intArrayContains(int currentState, int[] myArray) {
	    boolean found = false;

	    for (int i = 0; !found && (i < myArray.length); i++) {
	        found = (myArray[i] == currentState);
	    }

	    return found;
	}

	public void visualiseGrover(boolean firstTime) throws InterruptedException {
		if(!this.visualise){
			return;
		}
		
		double xc = 0;
		double yc = 0;
		
		FieldVector<Complex> temp1 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, 3));
		
		for(int i=0;i<(1<<this.arity+1);i=i+2){
			if(this.intArrayContains((int)Math.floor(i/2), this.solutions)){
				yc += Math.sqrt(2)*temp1.getEntry(i).getReal();
			} else {
				xc += Math.sqrt(2)*temp1.getEntry(i).getReal();
			}
		}
		
		xc /= Math.sqrt((1<<this.arity)-this.solutions.length);
		yc /= Math.sqrt(this.solutions.length);
		
		this.visualisation.vector.setComponents(xc, yc);
		if(firstTime){
			this.visualisation.initialStateLine.setComponents(xc, yc);
		}
		this.visualisation.repaint();
		
		Thread.sleep(this.visualisationDelayTime);
	}
	
	/**
	 * @throws InterruptedException 
	 * @throws Exception 
	 */
	@SuppressWarnings("static-access")
	public long doGrover() throws IllegalArgumentException, InterruptedException {
		
		this.arity=2;
		
		if(this.solutions == null){
			throw new IllegalArgumentException("Solutions list not provided");
		}
		
		this.qr = new QubitRegister(3);
		
		//Defining gates to be used multiple times
		Operator x, h, z;
		x = new X();
		h = new H();
		z = new Z();
		
		//Build the quantum register with first bit |1> and then 'arity' bits |0>
		this.qr.setAmps( QuantumUtil.buildVector(1,0), 2);
		this.qr.setAmps( QuantumUtil.buildVector(1,0), 1);
		this.qr.setAmps( QuantumUtil.buildVector(0,1), 0);
		
		//Apply H gate to every qubit
		this.qr.doOp(h, 2);
		this.qr.doOp(h, 1);
		this.qr.doOp(h, 0);
		
		this.visualiseGrover(true);
		
		//Perform Grover iteration		
		this.qr.doOp((new ComboOps()).toffoli(), 2, 1, 0);
			
		this.visualiseGrover(false);
			
		this.qr.doOp(h, 2);
		this.qr.doOp(h, 1);
		

		//Perform conditional phase shift
		this.qr.doOp(x, 1);
		this.qr.doOp(x, 2);
		
		this.qr.doOp(h, 1);
		
		this.qr.doOp(new CNOT(), 1, 2);
		
		
		this.qr.doOp(h, 1);
		
		this.qr.doOp(x, 1);
		this.qr.doOp(x, 2);
		
		/*
		 * The 4 operations below are basically redundant since the previous operations perform
		 * the correct phase shift up to a global phase shift of 180 degrees, but for the sake
		 * of visualization the 4 operations below were implemented to set the global phase shift
		 * to 0 so that the visualization would work properly 
		 */
		this.qr.doOp(z, 2);
		this.qr.doOp(x, 2);
		this.qr.doOp(z, 2);
		this.qr.doOp(x, 2);
		
		this.qr.doOp(h, 2);
		this.qr.doOp(h, 1);
			
		this.visualiseGrover(false);
				
		System.out.print(this.qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, 3)));
		
		//Measurement
		long result = 0;
		for(int i=1;i<3;i++){
			if(this.qr.measure(i)){
				result += (1 << (i-1));
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws InterruptedException {
		Grover2qubitWithBasicGates d = new Grover2qubitWithBasicGates(true);
		
		int[] solutions = new int[1];
		solutions[0] = 3;
		
		d.setSolutions(solutions);
		d.setVisualisationDelayTime(200);
		
		long result;
		
		result = d.doGrover();
		System.out.println("Result: " + result);
	}
}
