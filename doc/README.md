# Documentation folder

## Points for Report

1. Complex number arithmetic incurs performance overhead with frequent object creation and destruction.  See [Efficient Support for Complex Numbers in Java](http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.42.7264)
2. Chose to use `int[]` to index qubits so that we can maintain an ordered list of registers.  This allows one to use a CNOT gate on the 0th and 2nd qubit, for instance.  
If order didn't matter, [`BitSet`](http://docs.oracle.com/javase/7/docs/api/java/util/BitSet.html) would be a good choice to avoid reimplementing common bit twiddling algorithms.  Plus it allows variable length bit sets, as opposed to constraining the length of a bit set to 64 bits (the length of a `long`).
3. 