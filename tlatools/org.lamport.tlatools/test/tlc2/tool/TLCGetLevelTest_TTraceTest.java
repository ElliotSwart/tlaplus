/*******************************************************************************
 * Copyright (c) 2019 Microsoft Research. All rights reserved. 
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
import tlc2.output.EC.ExitStatus;
import tlc2.tool.liveness.TTraceModelCheckerTestCase;
import util.TTraceTest;

public class TLCGetLevelTest_TTraceTest extends TTraceModelCheckerTestCase {

	public TLCGetLevelTest_TTraceTest() {
		super(TLCGetLevelTest.class, ExitStatus.VIOLATION_LIVENESS);
	}

	@Category(TTraceTest.class)
	@Test
	public void testSpec() {
		assertTrue(recorder.recorded(EC.TLC_FINISHED));
		assertTrue(recorder.recordedWithStringValues(EC.TLC_STATS, "4", "4", "0"));
		assertFalse(recorder.recorded(EC.GENERAL));

		// Assert TLC has found a temporal violation and a counter example
		assertTrue(recorder.recorded(EC.TLC_TEMPORAL_PROPERTY_VIOLATED));
		assertTrue(recorder.recorded(EC.TLC_COUNTER_EXAMPLE));
		
		// Assert the error trace
		assertTrue(recorder.recorded(EC.TLC_STATE_PRINT2));
		final List<String> expectedTrace = new ArrayList<String>(4);
		expectedTrace.add("""
				/\\ yb = 0
				/\\ x = 0
				/\\ y = 0
				/\\ z = 1""");
		expectedTrace.add("""
				/\\ yb = 1
				/\\ x = 1
				/\\ y = 1
				/\\ z = 2""");
		expectedTrace.add("""
				/\\ yb = 2
				/\\ x = 2
				/\\ y = 2
				/\\ z = 3""");
		expectedTrace.add("""
				/\\ yb = 3
				/\\ x = 3
				/\\ y = 3
				/\\ z = 4""");
		assertTraceWith(recorder.getRecords(EC.TLC_STATE_PRINT2), expectedTrace);

		assertStuttering(5);
	}
}
