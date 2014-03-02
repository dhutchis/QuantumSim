package qclib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;
import org.apache.commons.math3.util.Pair;






import qclib.util.BitSetUtil;
import qclib.util.QuantumUtil;

public class QubitRegister {
	/** Number of qubits in the quantum register */
	private int numqubits;
	
	/** Array index is qubit number; maps to the qubit position in a QubitContainer. */
	private Pair<Integer,QubitContainer>[] qubitToQC;
	/** Reverse mapping of QubitContainer back to a list of integers representing the qubits in that container, in order */
	private Map<QubitContainer,int[]> QCToQubit;
	
	/**
	 * Initializes each qubit to state |0> inside separate qubit containers of size 1.
	 * The qubit containers are dense.
	 * @param numqubits
	 */
	@SuppressWarnings("unchecked")
	public QubitRegister(int numqubits) {
		this.numqubits = numqubits;
		qubitToQC = new Pair[numqubits];
		QCToQubit = new IdentityHashMap<QubitContainer,int[]>(numqubits); // compare by object identity using ==
		
		for (int i=0; i<numqubits; i++) {
			QubitContainer qc = new QubitContainer(1, false);
			qubitToQC[i] = new Pair<Integer,QubitContainer>(0, qc);
			QCToQubit.put(qc, new int[] {i});
		}
			
		
	}
	
	public int getNumqubits() { return numqubits; }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("qubitToQC (numqubits="+numqubits+")\n");
		/*for (int i=0; i<numqubits; i++) {
			
		}*/
		int i=0;
		for (Map.Entry<QubitContainer, int[]> entry : QCToQubit.entrySet()) {
			char QCchar = (char)('A'+i);
			sb.append("\t"+ QCchar +"->{");
			for (int q : entry.getValue())
				sb.append(q+",");
			sb.insert(sb.length()-1, '}');
			sb.append(" "+QCchar+":"+entry.getKey()+'\n');
			i++;
		}
		return sb.toString();
	}
	
	/** Are all the qubits inside the same container? Also error checks arguments. */
	private Set<QubitContainer> getContainersHolding(int... qubits) {
		if (qubits == null || qubits.length == 0 || qubits[0] < 0 || qubits[0] >= numqubits)
			throw new IllegalArgumentException("bad qubits");
		
		Set<QubitContainer> qcset = new HashSet<QubitContainer>(qubits.length); // max number of containers if each is in a separate one
		for (int i = 0; i < qubits.length; i++) {
			if (qubits[i] < 0 || qubits[i] >= numqubits)
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
	public void couple(int... qubits) { // make private later
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
		
		// let's create the vector to set to the new QubitContainer
		FieldVector<Complex> amps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<numBitsNew); // dense
		// initialize to all 1s
		amps.set(Complex.ONE);
		// now for each QubitContainer we're transferring to the new container
		int qcnewidx = 0;
		for (QubitContainer qc : qcset) {
			int[] qubitsTransferring = QCToQubit.get(qc);
			int[] qcnewidxarr = QuantumUtil.makeConsecutiveIntArray(qcnewidx, qubitsTransferring.length);
			
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
	
	// FUTURE TODO make a decouple method for decoupling unentangled QubitContainers
	// maybe make a method we can call on QubitContainers called isEntangled?
	// but need to distinguish which bits are entangled and which are not.
	
	/**
	 * Gets the qubit amplitudes from the underlying container.  
	 * If the qubits are in different containers, requires that no other non-argument qubits be in those containers.
	 *   In that case, does not couple the qubits.  Instead, returns a new vector with the amplitudes in proper order.  
	 * @param qubits the qubits of a single container
	 * @return The amplitudes, in order.
	 */
	public FieldVector<Complex> getAmps(int... qubits) {
		Set<QubitContainer> conts = getContainersHolding(qubits);
		QubitContainer qcTarget; 
		
		if (conts.size() == 1) {
			// case 1)
			qcTarget = conts.iterator().next();
			
			qcTarget = conts.iterator().next();
			if (qubits.length != qcTarget.getNumbits()) // need to specify all the qubits in the container
				throw new IllegalArgumentException("provided "+qubits.length+" qubits but they are in a container of size "+qcTarget.getNumbits());
			
			int[] targetbits = new int[qubits.length];
			for (int i=0; i<qubits.length; i++) {
				targetbits[i] = qubitToQC[qubits[i]].getFirst(); // the position of qubit[i] in qcTarget
				assert qubitToQC[i].getSecond() == qcTarget;
			}
			
			// translate from indices on qubits in the QR to indices on qubits in the QC
			Set<int[]> idxset = QuantumUtil.translateIndices(qubits.length, targetbits);
			assert idxset.size() == 1;
			int[] indices = idxset.iterator().next();
			
			FieldVector<Complex> amps = qcTarget.getAmps(),
					reorderedAmps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<qubits.length);
			QuantumUtil.indexGet(amps, indices, reorderedAmps);
			
			return reorderedAmps;
			
			
		} else {
			// check for case 2
			Set<Integer> qubitsSet = QuantumUtil.intArrayToSet(qubits);
			for (QubitContainer qc : conts) {
				for (int qInCont : QCToQubit.get(qc))
					if (!qubitsSet.contains(qInCont))  
						throw new IllegalStateException("case 3 not supported; qubit "+qInCont+" is not a target of setAmps but is in a container with another target. qubits="+qubitsSet);
			}
			// we have case 2 -- don't couple the containers together but temporarily put them together, using the order of qubits
			
			
			//QubitContainer qcnew = new QubitContainer(qubits.length, false); // dense
			// this is for the qubits that qcnew will map to
			int[] qubitsForqcnew = new int[qubits.length];
			
			// let's create the vector to set to the new QubitContainer
			FieldVector<Complex> amps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<qubits.length); // dense
			// initialize to all 1s
			amps.set(Complex.ONE);
			// now for each QubitContainer we're transferring to the new container
			int qcnewidx = 0;
			for (QubitContainer qc : conts) {
				int[] qubitsTransferring = QCToQubit.get(qc);
				int[] qcnewidxarr = QuantumUtil.makeConsecutiveIntArray(qcnewidx, qubitsTransferring.length);
				
				for (int[] indices : QuantumUtil.translateIndices(qubits.length, qcnewidxarr)) {
					QuantumUtil.indexMultiplyIn(amps, indices, qc.getAmps());	
				}
				
				// no update maps
				for (int i=0; i < qubitsTransferring.length; i++) {
					//qubitToQC[qubitsTransferring[i]] = new Pair<Integer,QubitContainer>(qcnewidx+i, qcnew);
					qubitsForqcnew[qcnewidx+i] = qubitsTransferring[i]; 
				}
				//QCToQubit.remove(qc);
				
				qcnewidx += qubitsTransferring.length;
			}
			assert qcnewidx == qubits.length;
			//QCToQubit.put(qcnew, qubitsForqcnew);
			
			// now reorder from order qubitsForqcnew to original qubits order
			int[] neworder = new int[qubits.length];
			for (int i=0; i<qubits.length; i++) {   // for each in original order
				for (int j=0; j<qubits.length; j++) // find index in new order
					if (qubitsForqcnew[i] == qubits[j]) {
						neworder[i] = j;
						break;
					}
			}
			
			Set<int[]> idxset = QuantumUtil.translateIndices(qubits.length, neworder);
			assert idxset.size() == 1;
			int[] indices = idxset.iterator().next();
			
			FieldVector<Complex> 
					reorderedAmps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<qubits.length);
			QuantumUtil.indexGet(amps, indices, reorderedAmps);
			
			// All Done xD
			return reorderedAmps;
		}
		
		
		
		
		
	}
	
	/**
	 * Naive implementation of setting amplitudes of qubits in the register.
	 * Cases:
	 * 1) Want to set n qubits, all n are in the same container.  OK!  Just match the order of the qubits to the order of the container.
	 * 2) Want to set n qubits, which are located in different containers, and there are no other qubits in those containers.
	 * 	  OK!  Couple the qubits and set the new container containing all the qubits to the provided amps.
	 * 3) Want to set n qubits, which are located in different containers, and there are other qubits in those containers.
	 * 	  Oh noes!  How do we handle the other qubits?  Just fail for now and work out a strategy later.
	 * @param amps vector of length 2^(qubits.length)
	 * @param qubits
	 * @return this.  Useful for chaining: QubitRegister qr.setAmps(amps1, {3}).setAmps(amps2, {1}).setAmps(...
	 */
	public QubitRegister setAmps(FieldVector<Complex> amps, int... qubits) {
		Set<QubitContainer> conts = getContainersHolding(qubits);
		QubitContainer qcTarget; // the SINGLE container whose amplitudes we will set 
		if (conts.size() == 1) {
			// case 1)
			qcTarget = conts.iterator().next();
		} else {
			// check for case 2
			Set<Integer> qubitsSet = QuantumUtil.intArrayToSet(qubits);
			for (QubitContainer qc : conts) {
				for (int qInCont : QCToQubit.get(qc))
					if (!qubitsSet.contains(qInCont))  
						throw new IllegalStateException("case 3 not supported; qubit "+qInCont+" is not a target of setAmps but is in a container with another target. qubits="+qubitsSet);
			}
			// we have case 2 -- couple the containers together and set them, adjusting the indices
			this.couple(qubits);
			
			conts = getContainersHolding(qubits);
			assert conts.size() == 1;
			qcTarget = conts.iterator().next();
		}
		
		if (qubits.length != qcTarget.getNumbits()) // need to specify all the qubits in the container
			throw new IllegalArgumentException("provided "+qubits.length+" qubits but they are in a container of size "+qcTarget.getNumbits());
		
		int[] targetbits = new int[qubits.length];
		for (int i=0; i<qubits.length; i++) {
			targetbits[i] = qubitToQC[qubits[i]].getFirst(); // the position of qubit[i] in qcTarget
			assert qubitToQC[i].getSecond() == qcTarget;
		}
		
		// translate from indices on qubits in the QR to indices on qubits in the QC
		Set<int[]> idxset = QuantumUtil.translateIndices(qubits.length, targetbits);
		assert idxset.size() == 1;
		int[] indices = idxset.iterator().next();
		
		FieldVector<Complex> reorderedAmps = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<qubits.length);
		QuantumUtil.indexSet(reorderedAmps, indices, amps);
		
		qcTarget.setAmps(reorderedAmps);
		
		return this;
	}
	
	/** Returns the first position of num in arr, or -1 if not present. (Sequential linear search) */
	private int getIndexInArray(int num, int[] arr) {
		for (int i=0; i<arr.length; i++)
			if (arr[i] == num)
				return i;
		return -1;
	}
	
	/**
	 * Returns the amplitudes of each qubit.  If in separate containers, prints the qubits in each container.
	 * Order matters: printBits(5,8) prints in the format |{8}{5}>.
	 * @param qubits
	 * @return
	 */
	public String printBits(int... qubits) {
		Set<QubitContainer> conts = getContainersHolding(qubits);
		QubitContainer qcTarget; // the SINGLE container we will print bits from
		if (conts.size() == 1) {
			// case 1)
			qcTarget = conts.iterator().next();
		}
		else {
			// multiple containers; call recursively
			String s = "";
			for (QubitContainer qc : conts)
				s += printBits(QCToQubit.get(qc));
			return s;
		}
		
		// print the bits in this container
		// qubits = the bits the caller wants to print
		// qubitsInTarget = the bits in the single container the caller wants to print from
		// map: for each qubit in container, map that qubit's position to the desired position in the new ordering
		// (new ordering starts with the order specified by user)
		
		int[] qubitsInTarget = QCToQubit.get(qcTarget);
		int[] map = new int[qubitsInTarget.length];
		int[] neworder = new int[qubitsInTarget.length];
		
		int freeidx = qubits.length; 
		assert freeidx <= qubitsInTarget.length;
		for (int i=0; i < qubitsInTarget.length; i++) {
			int bit = qubitsInTarget[i];
			int idx = getIndexInArray(bit, qubits); // desired position
			if (idx == -1) {
				idx = freeidx++;
				assert freeidx <= qubitsInTarget.length;
			}
			map[i] = idx;
		}
		
		// do the reordering and update the data structures
		qcTarget.reorderBits(map);
		for (int i=0; i < qubitsInTarget.length; i++) {
			int bit = qubitsInTarget[i];
			qubitToQC[bit] = new Pair<Integer,QubitContainer>(map[i],qcTarget);
			neworder[map[i]] = qubitsInTarget[i];
		}
		QCToQubit.put(qcTarget, neworder);
		
		// finally, print the desired String
		StringBuilder sb = new StringBuilder("{");
		for (int q : neworder)
			sb.append(q+",");
		sb.replace(sb.length()-1, sb.length(), "}");
		sb.append(":\n "+qcTarget);
		return sb.toString();
	}
	
	
	
	
	
	/**
	 * Measure a qubit. Collapses the qubit state afterward.
	 * @param targetbit which bit to measure
	 * @return False for |0>, True for |1>
	 */
	public boolean measure(int targetbit) {
		if (targetbit < 0 || targetbit >= numqubits)
			throw new IllegalArgumentException("bad targetbit");
		int bitInQC = qubitToQC[targetbit].getFirst();
		QubitContainer qc = qubitToQC[targetbit].getSecond();
		boolean meas = qc.measure(bitInQC);
		
		// if we measured a qubit container of size 1, don't need to decouple anything
		if (qc.getNumbits() == 1)
			return meas;
		
		// decouple afterward
		/* algorithm:
		 * 	get the entries excluding the measured bit, in their current order, with the bit set and without the bit set.
		 *  Check if the first set of entries is all zero.  If so, use the second.  If not, use the first.
		 *  Construct a new qubit container containing the remaining entries and map those qubits to the new container.
		 *  Construct a new qubit container of size 1 and map the one qubit to that container.
		 *    Put 1.0 amplitude in the |0> if !meas, 1.0 amplitude in the |1> if meas.
		 */
		
		// make an array of consecutive ints, except for the one measured
		int[] mask = new int[qc.getNumbits()-1];
		for (int i=0, j=0; i < qc.getNumbits(); ) {
			if (i != bitInQC)
				mask[j++] = i++;
			else
				i++;
		}
		System.out.println(Arrays.toString(mask)+" should be an array excluding "+bitInQC);
		
		Set<int[]> idxset = QuantumUtil.translateIndices(qc.getNumbits(), mask);
		assert idxset.size() == 2;
		int[] indicesA, indicesB;
		FieldVector<Complex> orig = qc.getAmps( ); // original entries in the original qubit container
		FieldVector<Complex> vecA = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<< qc.getNumbits()-1), 
				vecB = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<< qc.getNumbits()-1);
		{
			Iterator<int[]> iter = idxset.iterator();
			indicesA = iter.next();
			indicesB = iter.next();
			assert !iter.hasNext();
			
			QuantumUtil.indexGet(orig, indicesA, vecA);
			QuantumUtil.indexGet(orig, indicesB, vecB);
		}
//		System.out.println(QuantumUtil.printVector(vecA));
//		System.out.println(QuantumUtil.printVector(vecB));
		
		FieldVector<Complex> vecResult; // fill with the entries that came from the appropriate measurement
		{
			// is vecA the all zero vector?
			if (vecA.equals(new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<< qc.getNumbits()-1)))
				vecResult = vecB;
			else
				vecResult = vecA;
		}
		
		QubitContainer qcRemaining, qcMeasured;
		qcRemaining = new QubitContainer(qc.getNumbits()-1);
		qcRemaining.setAmps(vecResult);
		
		qcMeasured = new QubitContainer(1);
		FieldVector<Complex> vecMeasured = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<< 1);
		vecMeasured.setEntry(meas ? 1 : 0, Complex.ONE); // set 0th entry to 1 if measured a 0, set 1st entry to 1 if measured a 1
		qcMeasured.setAmps(vecMeasured);
		
		// change up the maps
		int[] previousBitsInQC = QCToQubit.get(qc);
		int[] newBitsInRemainingQC = new int[qc.getNumbits()-1]; assert qc.getNumbits() == previousBitsInQC.length;
		// exclude the measured bit from the remaining bits
		for (int i=0, j=0; i < qc.getNumbits(); ) {
			if (previousBitsInQC[i] != targetbit)
				newBitsInRemainingQC[j++] = previousBitsInQC[i++];
			else
				i++;
		}
		
		QCToQubit.put(qcRemaining, newBitsInRemainingQC);
		for (int i=0; i < newBitsInRemainingQC.length; i++)
			qubitToQC[newBitsInRemainingQC[i]] = new Pair<Integer,QubitContainer>(i, qcRemaining);
		
		// now for the measured bit
		QCToQubit.put(qcMeasured, new int[] {targetbit});
		qubitToQC[targetbit] = new Pair<Integer,QubitContainer>(0, qcMeasured);
		
		return meas;
	}
	
	/**
	 * Measure qubits in the order provided.  Collapses the qubit state after each measurement.
	 * @param targetbits bits to measure, in order
	 * @return boolean results, with order matching input
	 */
	public boolean[] measure(int... targetbits) {
		if (targetbits == null)
			throw new IllegalArgumentException("please no null's");
		boolean[] ret = new boolean[targetbits.length];
		for (int i=0; i < targetbits.length; i++)
			ret[i] = measure(targetbits[i]);
		return ret;
	}
	
	/**
	 * Perform an Operation on the specified qubits.  If they are not coupled, couple them.  
	 * If they are coupled with other elements, just couple everything together.
	 * @param op
	 * @param qubits
	 * @return this.  Useful for chaining: QubitRegsiter qr.setAmps(amps1, {2}).doOp(H,2).doOp(Z,2).doOp(...
	 */
	public QubitRegister doOp(Operator op, int... qubits) {
		/*Set<QubitContainer> conts = getContainersHolding(qubits);
		if (conts.size() > 1) {
			// need to do some coupling
			// first check to see if there are other qubits involved.  If so, panic.
			BitSet qubitset = new BitSet(numbits); // sorta slow; change later
			for (int q : qubits)
				qubitset.set(q);
			for (QubitContainer qc : conts)
				for (int q : QCToQubit.get(qc))
					if (!qubitset.get(q))
						throw new UnsupportedOperationException("coupled with other qubit "+q+"; didn't handle that yet");
			// OK, can safely couple and do the op
			this.couple(qubits);
		}*/
		
		// Change of plans: just couple everything together and do the op, perhaps on a bigger container than necessary
		couple(qubits); // no effect if already coupled
		QubitContainer qc = qubitToQC[qubits[0]].getSecond();
		int[] targetbits = new int[qubits.length];
		for (int i = 0; i < qubits.length; i++) {
			targetbits[i] = qubitToQC[qubits[i]].getFirst();
			assert qubitToQC[qubits[i]].getSecond() == qc;
		}
		qc.doOp(op, targetbits);
		return this;
	}
	

}
