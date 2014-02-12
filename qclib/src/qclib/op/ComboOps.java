package qclib.op;

import qclib.op.CNOT;
import qclib.op.CV;
import qclib.op.H;
import qclib.Operator;

public class ComboOps {

	/** Control bits 0,1; target bit 2 */
	public static Operator toffoli() {
		Operator H2 = new H().extend(3, 2);
		Operator CV12 = new CV().extend(3, 1, 2);
		Operator CV02 = new CV().extend(3, 0, 2);
		Operator CNOT01 = new CNOT().extend(3, 0, 1);
		Operator newop = H2.curryBefore(CV02)
				.curryBefore(CNOT01)
				.curryBefore(CV12)
				.curryBefore(CV12)
				.curryBefore(CV12)
				.curryBefore(CNOT01)
				.curryBefore(CV12)
				.curryBefore(H2);
		return newop;
	}

}
