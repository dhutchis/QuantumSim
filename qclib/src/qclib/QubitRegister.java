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
	
	/** Are all the qubits inside the same container? Also error checks arguments. */
	private boolean allSameContainer(int... qubits) {
		if (qubits == null || qubits.length == 0 || qubits[0] < 0 || qubits[0] >= numbits)
			throw new IllegalArgumentException("bad qubits");
		
		QubitContainer qc = regmap[qubits[0]].getSecond();
		for (int i = 1; i < qubits.length; i++) {
			if (qubits[i] < 0 || qubits[i] >= numbits)
				throw new IllegalArgumentException("bad qubit: "+qubits[i]);
			if (qc != regmap[qubits[i]].getSecond()) // compare for object identity
				return false;
		}
		return true;
	}
	
	/**
	 * Combines qubits into a single, larger (dense) container. 
	 * Note: if a qubit is in a container with an unspecified qubit, it needs to be included too
	 * 		(unless they are unentangled -- future todo) 
	 * Future: accept a BitSet argument, since the order does not matter
	 * @param qubits The qubits to combine into a single QubitContainer of size qubits.length
	 */
	private void couple(int... qubits) {
		// see if they are already part of the same container
		if (allSameContainer(qubits))
			return;
		// at least one qubit is in a different container
		QubitContainer qcnew = new QubitContainer(qubits.length, false); // dense
		// TODO set the qubits in qcnew according to the qubits in the other containers
	}
	
	// FAR FUTURE TODO make a decouple method for decoupling unentangled QubitContainers
	// maybe make a method we can call on QubitContainers called isEntangled?
	// but need to distinguish which bits are entangled and which are not.
	
	public QubitRegister setAmps(FieldVector<Complex> amps, int... qubits) {
		// TODO
		
		return this;
	}
	

}
