// Copyright (c) 2012 Markus Alexander Kuppe. All rights reserved.
package tlc2.tool.distributed.fp.callable;

import java.util.List;

import tlc2.tool.distributed.fp.FPSetManager;
import tlc2.tool.distributed.fp.FPSetManager.FPSets;
import tlc2.util.BitVector;
import tlc2.util.LongVec;

public class PutBlockCallable extends FPSetManagerCallable {
	
	public PutBlockCallable(final FPSetManager fpSetManager, final List<FPSets> fpSets, final LongVec[] fps, final int index) {
		super(fpSetManager, fpSets, fps, index);
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
    public BitVectorWrapper call() throws Exception {
		try {
			final BitVector bv = fpset.get(index).putBlock(fps[index]);
			return new BitVectorWrapper(index, bv);
		} catch (final Exception e) {
			return reassign(e);
		}
	}
}