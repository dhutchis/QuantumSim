package qclib.alg;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.H;
import qclib.util.QuantumUtil;
import qclib.util.CartesianRepresentation;

public class GroverWithoutOracleWorkingQubit {
	
	private QubitRegister qr;
	private CartesianRepresentation visualisation;
	private int arity;
	private boolean visualise;
	private int[] solutions;
	private int visualisationDelayTime;
	
	static class SpecialF extends Operator {

		private FunctionFilter funct;
		
		public SpecialF(FunctionFilter function, int arity) {

			super(arity);
			assert arity >= 2;
			this.funct = function;
		}

		@Override
		protected FieldVector<Complex> myApply(FieldVector<Complex> invec) {
			FieldVector<Complex> outvec = new ArrayFieldVector<Complex>(ComplexField.getInstance(), invec.getDimension());
			outvec.set(Complex.ZERO);
			
			for (int x = 0; x < 1<<this.getArity(); x++) {
				if (this.funct.apply(x))
					outvec.setEntry(x, invec.getEntry(x).negate());
				else
					outvec.setEntry(x, invec.getEntry(x));
			}
			
			return outvec;
		}
		
	}
	
	public GroverWithoutOracleWorkingQubit(){
		this.setVisualisation(false);
		this.setVisualisationDelayTime(500);
	}
	
	public GroverWithoutOracleWorkingQubit(boolean visualise){
		this.setVisualisation(visualise);
		this.setVisualisationDelayTime(500);
	}
	
	public GroverWithoutOracleWorkingQubit(int visualisationDelayTime){
		this.setVisualisation(true);
		this.setVisualisationDelayTime(visualisationDelayTime);
	}
	
	public GroverWithoutOracleWorkingQubit(boolean visualise, int visualisationDelayTime){
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
		
		FieldVector<Complex> temp1 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, this.arity));
		
		for(int i=0;i<(1<<this.arity);i++){
			if(intArrayContains(i, this.solutions)){
				yc += temp1.getEntry(i).getReal();
			} else {
				xc += temp1.getEntry(i).getReal();
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
	 * @throws Exception 
	 */
	public long doGrover(final int arity, FunctionFilter funct) throws Exception {
		assert arity > 1;
		
		this.arity = arity;
		
		if(this.solutions == null){
			throw new Exception("Solutions list not provided");
		}
		
		if(this.solutions.length >= (1<<this.arity)/2){
			return doGrover(arity+1, funct);
		}
		
		this.qr = new QubitRegister(arity);
		
		//Build the quantum register with all the bits |0>
		for(int i=0;i<this.qr.getNumqubits();i++){
			this.qr.setAmps( QuantumUtil.buildVector(1,0), i);
		}
		
		//Apply H gate to every qubit
		for(int i=0;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
		
		this.visualiseGrover(true);
		
		SpecialF search = new SpecialF(funct, arity);
		
		//Perform Grover iterations
		for(int j=1;j<Math.ceil(Math.PI/4*Math.sqrt((1 << arity)/(double) this.solutions.length));j++){
			
			this.qr.doOp(search, QuantumUtil.makeConsecutiveIntArray(0, arity));
			
			this.visualiseGrover(false);
			
			for(int i=0;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
			
			FieldVector<Complex> temp2 = this.qr.getAmps(QuantumUtil.makeConsecutiveIntArray(0, arity));			
			for(int i=1;i<(1<<arity);i++){
				temp2.setEntry(i, temp2.getEntry(i).negate());
			}
			
			this.qr.setAmps(temp2, QuantumUtil.makeConsecutiveIntArray(0, arity));
			
			for(int i=0;i<this.qr.getNumqubits();i++){this.qr.doOp(new H(), i);}
			
			this.visualiseGrover(false);
		}
		
		System.out.print(this.qr.printBits(QuantumUtil.makeConsecutiveIntArray(0, arity)));
		
		//Measurement
		long result = 0;
		for(int i=0;i<arity;i++){
			if(this.qr.measure(i)){
				result += (1 << i);
			}
		}
		return result;
	}
	
	private static class Find implements FunctionFilter {
		private int[] toFind;
		
		public Find(int[] intsToFind){
			this.toFind = intsToFind;
		}
		
		@Override
		public boolean apply(int argument){
			if(intArrayContains(argument,this.toFind)){
				return true;
			}
			return false;
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		GroverWithoutOracleWorkingQubit d = new GroverWithoutOracleWorkingQubit(true);
		
		int[] solutions = new int[1];
		solutions[0] = 0;
		
		d.setSolutions(solutions);
		d.setVisualisationDelayTime(1);
		
		long result;
		
		try {
			result = d.doGrover(3, new Find(solutions));
			System.out.println("Result: " + result);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
}
