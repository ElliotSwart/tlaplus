/*******************************************************************************
 * Copyright (c) 2015 Microsoft Research. All rights reserved. 
 *
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/

package tlc2.tool.liveness;

import java.io.IOException;

import tlc2.output.EC;
import tlc2.output.MP;
import tlc2.tool.EvalException;
import tlc2.tool.ITool;
import tlc2.tool.TLCState;
import tlc2.tool.Worker;
import tlc2.util.SetOfStates;
import tlc2.util.statistics.IBucketStatistics;

/**
 * {@link AddAndCheckLiveCheck} is a subclass of LiveCheck that always runs
 * liveness checking after each and every insertion of a new node into the
 * behavior graph. It is meant for TESTING ONLY!!! as it severely
 * degrades TLC's performance/scalability.
 * <p>
 * The key difference between {@link AddAndCheckLiveCheck} and {@link LiveCheck}
 * is that both methods are synchronized to only allow one {@link Worker} to add
 * a state at a time and that liveness checking is after every insertion.
 */
public class AddAndCheckLiveCheck extends LiveCheck {

	public AddAndCheckLiveCheck(final ITool tool, final String metadir, final IBucketStatistics stats) throws IOException {
		super(tool.getLiveness(), metadir, stats);
		MP.printWarning(EC.UNIT_TEST, "!!!WARNING: TLC is running in inefficient unit testing mode!!!", "");
	}

	/* (non-Javadoc)
	 * @see tlc2.tool.liveness.LiveCheck#addInitState(tlc2.tool.TLCState, long)
	 */
	@Override
	public synchronized void addInitState(final ITool tool, final TLCState state, final long stateFP) {
		super.addInitState(tool, state, stateFP);
		try {
			int result = check0(tool, false);
			if (result != 0){
				throw new EvalException(EC.TLC_TEMPORAL_PROPERTY_VIOLATED);
			}
		} catch (final InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see tlc2.tool.liveness.LiveCheck#addNextState(tlc2.tool.TLCState, long, tlc2.util.SetOfStates)
	 */
	@Override
	public synchronized void addNextState(final ITool tool, final TLCState s0, final long fp0, final SetOfStates nextStates) throws Exception {
		super.addNextState(tool, s0, fp0, nextStates);
		try {
			int result = check0(tool, false);
			if (result != 0){
				throw new EvalException(EC.TLC_TEMPORAL_PROPERTY_VIOLATED);
			}

		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
