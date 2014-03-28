package qclib.alg;

import org.apache.commons.math3.complex.Complex;

import qclib.QubitRegister;
import qclib.op.CNOT;
import qclib.op.H;
import qclib.op.OpMaker;
import qclib.op.Z;
import qclib.util.QuantumUtil;

public class Teleport {

	/**
	 * Operates on three qubits in a qr: srcBit, bellBit1, bellBit2.
	 * Teleports the state of srcBit in qr to bellBit2.
	 * bellBit1 and bellBit2 should be in the state (|00> + |11>)/sqrt(2).
	 * In the classic example, Alice owns qubit one and the first half of the entanlged bellBit pair.
	 * 	Bob owns the second half of the entanlged bellBit pair.
	 * 	Bob can learn the state of Alice's srcBit (though it will destroy her srcBit) by teleporting it through the entanlgled bellBits. 
	 *  Alice need only transmit the result of a measurement of her srcBit and the first bellBit1 to Bob via classical means.
	 * @param qr QubitRegister
	 * @param srcBit The qubit to teleport
	 * @param bellBit1 The 
	 * @param bellBit2
	 */
	public static void doTeleport(QubitRegister qr, int srcBit, int bellBit1, int bellBit2) {
		// put bellBit1 and bellBit2 in a Bell00 state 
		qr.setAmps( QuantumUtil.buildVector(1,0) , 1) 	// bellBit1 is |0>
		 .setAmps( QuantumUtil.buildVector(1,0) , 2); 	// bellBit2 is |0>
		qr.doOp(new H(), 1); 							// bellBit1 is (|0>+|1>) / sqrt(2)
		qr.doOp(new CNOT(), 2, 1); //control 1, target 2;  bellBits are (|00> + |11>) / sqrt(2)
		System.out.println("qr before teleport:\n"+qr.printBits(2,1,0));
		
		System.out.println("Alice performs CNOT on srcBit and her half of the bellBits, and then H on her half of the bellBits...");
		qr.doOp(new CNOT(), 1, 0);
		System.out.println("qr after CNOT01: "+qr.printBits(2,1,0));
		qr.doOp(new H(), 0);
		System.out.println("qr after CNOT01 and H:\n"+qr.printBits(2,1,0));
		
		boolean m0 = qr.measure(0);
		boolean m1 = qr.measure(1);
		System.out.println("Measurement: srcBit="+m0+"; bellBit1="+m1);
		System.out.println("qr after measurment:\n"+qr.printBits(2,1,0) );
		
		System.out.println("Alice sends result to Bob...");
		if (m1) {
			System.out.println("Bob performs X on his bellBit2 since Alice measured bellBit1 == |1>");
			qr.doOp(OpMaker.makeX(), 2);
		}
		if (m0) {
			System.out.println("Bob performs Z on his bellBit2 since Alice measured srcBit == |1>");
			qr.doOp(new Z(), 2);
		}
		
		System.out.println("Bob now has a copy of the original srcBit in bellBit2");
		int idx = (m0 ? 0b100 : 0) | (m1 ? 0b010 : 0);
		Complex z0 = qr.getAmps(2,1,0).getEntry(idx);
		Complex z1 = qr.getAmps(2,1,0).getEntry(idx | 0b001);
		
		System.out.println("Coefficient of |0>: "+z0);
		System.out.println("Coefficient of |1>: "+z1);
	}

	
	public static void main(String[] args) {
		QubitRegister qr = new QubitRegister(4);
		qr.setAmps(QuantumUtil.buildVector(3.0/5, 4.0/5), 0); // state to transmit is (3/5)|0> + (4/5)|1>
		Teleport.doTeleport(qr, 0, 1, 2);
		//System.out.println(QuantumUtil.printVector(qr.getAmps(2,1,0)));
	}
}
