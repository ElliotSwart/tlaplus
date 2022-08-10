/*******************************************************************************
 * Copyright (c) 2017 Microsoft Research. All rights reserved. 
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
package tlc2.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.junit.experimental.categories.Category;
import tlc2.output.EC;
import tlc2.tool.liveness.ModelCheckerTestCase;
import util.IndependentlyRunTest;

public class DistributedTraceTest extends ModelCheckerTestCase {

	public DistributedTraceTest() {
		super("DistributedTrace", EC.ExitStatus.VIOLATION_SAFETY);
	}

	@Test
	public void testSpec() {
		assertTrue(recorder.recorded(EC.TLC_FINISHED));
		assertFalse(recorder.recorded(EC.GENERAL));

		//assertNoTESpec();
		
		// Assert the error trace
		assertTrue(recorder.recorded(EC.TLC_STATE_PRINT2));
		final List<String> expectedTrace = new ArrayList<String>(5);
		expectedTrace.add("x = 10");
		expectedTrace.add("x = 11");
		expectedTrace.add("x = 12");
		expectedTrace.add("x = 13");
		expectedTrace.add("x = 14");
		expectedTrace.add("x = 15");
		expectedTrace.add("x = 16");
		expectedTrace.add("x = 17");
		expectedTrace.add("x = 18");
		expectedTrace.add("x = 19");
		expectedTrace.add("x = 20");
		assertTraceWith(recorder.getRecords(EC.TLC_STATE_PRINT2), expectedTrace);
	}

	/* (non-Javadoc)
	 * @see tlc2.tool.liveness.ModelCheckerTestCase#getNumberOfThreads()
	 */
	@Override
    protected int getNumberOfThreads() {
		return 4;
	}
}
