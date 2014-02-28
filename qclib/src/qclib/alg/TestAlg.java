package qclib.alg;

import java.util.BitSet;

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
		qr.setAmps(QuantumUtil.buildVector(3.0/5, 4.0/5), 10); // qubit 10 is (3/5)|0> + (4/5)|1>
		qr.setAmps(QuantumUtil.buildVector(3.0/5, -4.0/5), 11); // qubit 11 is (3/5)|0> - (4/5)|1>
		System.out.println(qr.printBits(10,11));
		
		qr.couple(10,11);
		System.out.println(qr.printBits(10,11));
		
		qr.doOp(new CNOT(),	10, 11); // target bit 10, control bit 11
		System.out.println(qr.printBits(10,11));
		
		boolean meas = qr.measure(10);
		System.out.println("measured a "+(meas ? 1 : 0) +" on qubit 10");
		
		//System.out.println(QuantumUtil.printVector(qr.getAmps(10)));
		System.out.println(qr.printBits(10,11));
	}
	
	
	
	
	

}
