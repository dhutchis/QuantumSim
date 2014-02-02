package qclib.util;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexField;
import org.apache.commons.math3.linear.ArrayFieldVector;
import org.apache.commons.math3.linear.FieldVector;

import qclib.Operator;

public class QuantumUtil {
	
	/** Tolerance for double equality checking */
	public final static double EPSILON = 0.00001;
	public final static boolean isApproxZero(double arg) {
		return arg < EPSILON && arg > -EPSILON;
	}
	
	public final static double square(double a) { return a*a; }

	public final static int[] integerToIntArray(Integer[] in) {
		int[] ret = new int[in.length];
		for (int i=0; i<in.length; i++)
			ret[i] = in[i];
		return ret;
	}
	
	public final static boolean isApproxEqualComplex(Complex x, Complex y) {
		return isApproxZero(x.getReal()-y.getReal()) &&
				isApproxZero(x.getImaginary()-y.getImaginary());
	}
	
	
	/// ---------------
	/// Index Functions
	/// ---------------

	/** Interface for a callback function, which is passed a BitSet for each possible freebit combination */
	public static interface BitSetCallback<T> {
		public T doWithFreeBitsSet(BitSet freebitset);
	}
	
	/**
	 * Example:
	 * 	freebitmask = 0101
	 * 	callback is called 4 times with these arguments (in some order):
	 * 		0000
	 * 		0001
	 * 		0100
	 * 		0101
	 * 
	 * @param freebitmask
	 * @param callback
	 * @return
	 */
	public static <T> Set<T> doForEachFreeBitSet(final BitSet freebitmask, final BitSetCallback<T> callback) {
		Set<T> ret = new HashSet<T>(freebitmask.cardinality());
		doForEachFreeBitSet_Help(freebitmask, callback, ret, 0, new BitSet(freebitmask.size()));
		return ret;
	}
	
	/** output argument ret */
	private static <T> void doForEachFreeBitSet_Help(final BitSet freebitmask, final BitSetCallback<T> callback, Set<T> ret, int idxAt, BitSet freebits) {
		idxAt = freebitmask.nextSetBit(idxAt);
		if (idxAt == -1) {
			ret.add( callback.doWithFreeBitsSet(freebits) );
			return;
		}
		doForEachFreeBitSet_Help(freebitmask, callback, ret, idxAt, freebits); // recurse with a 0 at freebit idxAt
		freebits.set(idxAt);
		doForEachFreeBitSet_Help(freebitmask, callback, ret, idxAt, freebits); // recurse with a 1 at freebit idxAt
		freebits.clear(idxAt);
		return;
	}
	
	
	
	/**<pre>
	 * Translate indices on a vector v1 of length 2^v1loglen
	 * 		to indices on a vector v2 of length 2^targetbits.length
	 * 		where the indices on v2 correspond to the indices on v1 as specificed by targetbits.
	 * 
	 * Examples:
	 * v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {2,0}, targetbits.length = 2
	 * Returns: Set({000, 100, 001, 101},  //corresponds to {00, 01, 10, 11} of v2
	 * 			 	{010, 110, 011, 111})  //corresponds to {00, 01, 10, 11} of v2
	 * 
	 * v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {1}, targetbits.length = 1
	 * Returns: Set({000, 010},  //corresponds to {0, 1} of v2
	 * 				{001, 011},  //corresponds to {0, 1} of v2
	 * 				{100, 110},  //corresponds to {0, 1} of v2
	 * 			 	{101, 111})  //corresponds to {0, 1} of v2
	 * 
	 * v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {0,2,1}, targetbits.length = 3
	 * Returns: Set({000, 001, 100, 101, 010, 011, 110, 111})
	 * Note: v1 and v2 have same sizes, but we reordered the indices
	 * </pre>
	 * @param v1loglen log2(length of v1)
	 * @param targetbits the bits, in order, we want v2 to map to
	 * @return Set of indices in the original v1
	 */
	public static Set<int[]> translateIndices(int v1loglen, final int... targetbits) {
		if (v1loglen < 2 || targetbits == null || targetbits.length > v1loglen)
			throw new IllegalArgumentException("bad v1loglen="+v1loglen+", targetbits "+targetbits+(targetbits==null?"":" with length "+targetbits));

		// running example: v1 = {000,001,010,011,100,101,110,111}, v1loglen = 3, targetbits = {2,0}, targetbits.length = 2
		//int[][] ret = new int[1<<(v1loglen-targetbits.length)][1<<(targetbits.length)];
	//	Set<int[]> ret = new HashSet<int[]>(1<<(v1loglen-targetbits.length));
		// ret is a (2^1)x(2^2) = 2x4 array
		// expected result:	{{000, 100, 001, 101},
		//					 {010, 110, 011, 111}} // order of the two arrays doesn't matter
		final BitSet freebitmask = new BitSet(v1loglen);
		freebitmask.set(0, v1loglen-1);
		// freebitmask starts at 111
		// now change it to 010, since bit 1 is free and bits 2, 0 are fixed, meaning not specified in targetbits
		for (int tb : targetbits) {
			if (tb < 0 || tb >= v1loglen) // error check
				throw new IllegalArgumentException("bad v1loglen="+v1loglen+", targetbits "+targetbits);
			freebitmask.clear(tb); 
		}
		
		// Do this operation on each free bit:
		BitSetCallback<int[]> callback = new BitSetCallback<int[]>() {
			@Override
			public int[] doWithFreeBitsSet(BitSet freebitset) {
				int[] arr = new int[1<<(targetbits.length)]; // size 2^(targetbits.length)
				
				// turn freebitset into an int -- later TODO change everything to work with BitSets
				// init each element of arr to have the free bits set  
				long[] freebits = freebitmask.toLongArray();
				assert freebits.length==1 : "using more than 64 bits not currently supported";
				assert freebits[0] >>> 32 == 0 : "using more than 32 bits not currently supported";
				for (int i=0; i < targetbits.length; i++)
					arr[i] = (int)freebits[0];
				
				// now change the elements of arr to conform to the targetbits
				for (int i = 0; i < targetbits.length; i++) {
					int arridx = 0;			// index into arr
					int setlen = 1<<i;		// period of this target; double with each targetbit
					int k = 0;				// counting to period
					boolean setOne = false; // setting 0 or 1
					while (arridx < arr.length) {
						if (setOne)
							arr[arridx] |= 1<<targetbits[i]; // set targetbit at arridx
						arridx++;
						k++;
						if (k == setlen) {		// period elapsed; switch setOne
							setOne = !setOne;
							k = 0;
						}
					}
				}
				return arr;
			}
		}; // end callback function
		
		Set<int[]> result = doForEachFreeBitSet(freebitmask, callback);
		assert result.size() == 1<<(v1loglen-targetbits.length);
		return result;
	}
	
	
	
	/**
	 * Places the amplitudes from v1 into v1part, mapped by indices.  
	 * Take indices from translateIndices(*log2(length of v1)*, *targetbits*)
	 * @param v1
	 * @param indices
	 * @param v1part with entries set.  Assumes v1part.getDimension() == indices.length
	 */
	public static void indexGet(final FieldVector<Complex> v1, final int[] indices, FieldVector<Complex> v1part) {
		
		for (int i=0; i < indices.length; i++)
			v1part.setEntry(i, v1.getEntry(indices[i]));
	}
	
	/**
	 * Places the amplitudes from v1part in v1, mapped by indices. 
	 * @param v1 with some of the entries set from v1part
	 * @param indices
	 * @param v1part
	 */
	public static void indexSet(FieldVector<Complex> v1, final int[] indices, final FieldVector<Complex> v1part) {
		if (v1part.getDimension() != indices.length)
			throw new IllegalArgumentException("v1part.getDimension()="+v1part.getDimension()+", indices.length="+indices.length);
		for (int i=0; i < indices.length; i++)
			v1.setEntry(indices[i], v1part.getEntry(i));
	}
	
	/**
	 * Similar to indexSet(), but multiplies in the values of v1part into v1, in the places specified by indices.
	 * @param v1 with some of the entries multiplied by those from v1part
	 * @param indices
	 * @param v1part
	 */
	public static void indexMultiplyIn(FieldVector<Complex> v1, final int[] indices, final FieldVector<Complex> v1part) {
		if (v1part.getDimension() != indices.length)
			throw new IllegalArgumentException("v1part.getDimension()="+v1part.getDimension()+", indices.length="+indices.length);
		for (int i=0; i < indices.length; i++)
			v1.setEntry( indices[i], v1.getEntry(indices[i]).multiply(v1part.getEntry(i)) );
	}

	/**
	 * Performs the Operator on given datavec and stores the result in datavec.
	 * Length of datavec is 2^datavecloglength
	 * @param op
	 * @param data
	 * @param targetbits
	 */
	public static void doOp(Operator op, int datavecloglength, FieldVector<Complex> datavec, int... targetbits) {
		if (targetbits == null || datavec == null || op == null || targetbits.length > datavecloglength)
			throw new IllegalArgumentException("bad arguments to QuantumUtil.doOp");
		if (datavecloglength != datavec.getDimension())
			System.err.println("Warning: datavecloglength="+datavecloglength+", datavec.getDimension()="+datavec.getDimension());
		
		// vector to hold the amplitudes to pass to the operator
		FieldVector<Complex> vec = new ArrayFieldVector<Complex>(ComplexField.getInstance(),targetbits.length);
		// map indices in this.data to indices in vec, in order specified by targetbits
		Set<int[]> indexset = QuantumUtil.translateIndices(datavecloglength, targetbits);
		// for each set of indices indexing into this.data
		for (int[] indices : indexset) {
			// get the amplitudes from this.data into vec, do the operator on vec to get a new vec, and set the new amplitudes from vec into this.data 
			QuantumUtil.indexGet(datavec, indices, vec);
			vec = op.apply(vec);
			QuantumUtil.indexSet(datavec, indices, vec);
		}
	}
	
	/** log base 2 of a positive number */
	public static final int log2(int num) {
		if (num <= 0)
			throw new IllegalArgumentException("num "+num+" should be >= 0");
		int log2 = -1;
		while (num != 0) {
			num >>= 1;
			log2++;
		}
		return log2;
	}
	
	/** Example: makeIntArrayStartEnd(3,5) returns {3, 4, 5, 6, 7} */
	public static int[] makeIntArrayStartLen(int firstval, int len) {
		if (len <= 0)
			throw new IllegalArgumentException("bad len="+len);
		int[] ret = new int[len];
		for (int i=0; i<ret.length; i++)
			ret[i] = firstval+i;
		return ret;
	}
	
}
