package qclib.alg;

import java.util.BitSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;
import qclib.QubitRegister;
import qclib.op.CNOT;
import qclib.util.QuantumUtil;

public class TestAlg {

	public static void main(String[] args) {
		QubitRegister qr = new QubitRegister(15);
		qr.setAmps(QuantumUtil.buildVector(0, 1, 2, 3, 4, 5, 6, 7), 10, 11, 12); // qubit 10 is (3/5)|0> + (4/5)|1>
		System.out.println(qr.printBits(10,11,12));
		//System.out.println(QuantumUtil.printVector(qr.getAmps(10,11,12)));
		
		boolean meas1 = qr.measure(11);
		System.out.println("measured a "+(meas1 ? 1 : 0) +" on qubit 11");
		System.out.println(qr.printBits(10,11,12));
		
		Set<int[]> idxset = QuantumUtil.translateIndices(3, 0, 2);
		assert idxset.size() == 2;
		int[] indicesA, indicesB;
		FieldVector<Complex> orig = qr.getAmps(10,11,12);
		FieldVector<Complex> vecA = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<3), 
				vecB = new ArrayFieldVector<Complex>(ComplexField.getInstance(), 1<<3);;
		{
			Iterator<int[]> iter = idxset.iterator();
			indicesA = iter.next();
			indicesB = iter.next();
			assert !iter.hasNext();
			
			QuantumUtil.indexGet(orig, indicesA, vecA);
			QuantumUtil.indexGet(orig, indicesB, vecB);
		}
		System.out.println(QuantumUtil.printVector(vecA));
		System.out.println(QuantumUtil.printVector(vecB));
		
	}
	
	
	
	
	

}
