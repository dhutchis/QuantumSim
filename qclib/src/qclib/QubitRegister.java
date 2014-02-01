package qclib;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.util.Pair;

public class QubitRegister {
	/** Number of qubits in the quantum register */
	private int numbits;
	
	private Pair<Integer,QubitContainer>[] regmap;
	
	/**
	 * Initializes each qubit to state |0> inside separate qubit containers of size 1.
	 * The qubit containers are dense.
	 * @param numbits
	 */
	@SuppressWarnings("unchecked")
	public QubitRegister(int numbits) {
		this.numbits = numbits;
		regmap = new Pair[numbits];
		
		for (int i=0; i<numbits; i++)
			regmap[i] = new Pair<Integer,QubitContainer>(0, new QubitContainer(1, false));
		
	}
	
	public int getNumbits() { return numbits; }
	
	// TODO make a couple method for combining QubitContainers
	
	// FAR FUTURE TODO make a decouple method for decoupling unentangled QubitContainers
	// maybe make a method we can call on QubitContainers called isEntangled?
	// but need to distinguish which bits are entangled and which are not.
	
	public QubitRegister setAmps(FieldVector<Complex> amps, int... qubits) {
		// TODO
		
		return this;
	}
	

}
