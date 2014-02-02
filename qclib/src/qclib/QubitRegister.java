package qclib;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.util.Pair;

import qclib.util.QuantumUtil;

public class QubitRegister {
	/** Number of qubits in the quantum register */
	private int numbits;
	
	/** Array index is qubit number; maps to the qubit position in a QubitContainer. */
	private Pair<Integer,QubitContainer>[] qubitToQC;
	/** Reverse mapping of QubitContainer back to a list of integers representing the qubits in that container, in order */
	private Map<QubitContainer,int[]> QCToQubit;
	
	/**
	 * Initializes each qubit to state |0> inside separate qubit containers of size 1.
	 * The qubit containers are dense.
	 * @param numbits
	 */
	@SuppressWarnings("unchecked")
	public QubitRegister(int numbits) {
		this.numbits = numbits;
		qubitToQC = new Pair[numbits];
		QCToQubit = new IdentityHashMap<QubitContainer,int[]>(numbits); // compare by object identity using ==
		
		for (int i=0; i<numbits; i++) {
			QubitContainer qc = new QubitContainer(1, false);
			qubitToQC[i] = new Pair<Integer,QubitContainer>(0, qc);
			QCToQubit.put(qc, new int[] {i});
		}
			
		
	}
	
	public int getNumbits() { return numbits; }
	
	/** Are all the qubits inside the same container? Also error checks arguments. */
	private Set<QubitContainer> getContainersHolding(int... qubits) {
		if (qubits == null || qubits.length == 0 || qubits[0] < 0 || qubits[0] >= numbits)
			throw new IllegalArgumentException("bad qubits");
		
		Set<QubitContainer> qcset = new HashSet<QubitContainer>(qubits.length); // max number of containers if each is in a separate one
		for (int i = 0; i < qubits.length; i++) {
			if (qubits[i] < 0 || qubits[i] >= numbits)
				throw new IllegalArgumentException("bad qubit: "+qubits[i]);
			qcset.add(qubitToQC[qubits[i]].getSecond());
		}
		return qcset;
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
		Set<QubitContainer> qcset = getContainersHolding(qubits);
		if (qcset.size() == 1)
			return;
		// at least one qubit is in a different container
		// how many total qubits do we need to put in a new container?
		int numBitsNew = 0;
		for (QubitContainer qc : qcset) {
			numBitsNew += QCToQubit.get(qc).length; // invariant: integers are mutually exclusive
		}
		QubitContainer qcnew = new QubitContainer(numBitsNew, false); // dense
		// this is for the qubits that qcnew will map to
		int[] qubitsForqcnew = new int[numBitsNew];
		
		// TODO set the qubits in qcnew according to the qubits in the other containers
		// let's create the vector to set to the new QubitContainer
		FieldVector<Complex> amps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<numBitsNew); // dense
		// initialize to all 1s
		amps.set(Complex.ONE);
		// now for each QubitContainer we're transferring to the new container
		int qcnewidx = 0;
		for (QubitContainer qc : qcset) {
			int[] qubitsTransferring = QCToQubit.get(qc);
			int[] qcnewidxarr = QuantumUtil.makeIntArrayStartLen(qcnewidx, qubitsTransferring.length);
			
			for (int[] indices : QuantumUtil.translateIndices(numBitsNew, qcnewidxarr)) {
				QuantumUtil.indexMultiplyIn(amps, indices, qc.getAmps());	
			}
			
			// update maps
			for (int i=0; i < qubitsTransferring.length; i++) {
				qubitToQC[qubitsTransferring[i]] = new Pair<Integer,QubitContainer>(qcnewidx+i, qcnew);
				qubitsForqcnew[qcnewidx+i] = qubitsTransferring[i]; 
			}
			QCToQubit.remove(qc);
			
			qcnewidx += qubitsTransferring.length;
		}
		assert qcnewidx == numBitsNew;
		QCToQubit.put(qcnew, qubitsForqcnew);
		// All Done xD
		qcnew.setAmps(amps);
	}
	
	// FAR FUTURE TODO make a decouple method for decoupling unentangled QubitContainers
	// maybe make a method we can call on QubitContainers called isEntangled?
	// but need to distinguish which bits are entangled and which are not.
	
	public QubitRegister setAmps(FieldVector<Complex> amps, int... qubits) {
		// TODO
		
		return this;
	}
	

}
