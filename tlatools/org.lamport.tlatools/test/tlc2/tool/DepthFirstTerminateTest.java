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

import org.junit.Ignore;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import tlc2.output.EC;
import tlc2.tool.liveness.ModelCheckerTestCase;
import util.IndependentlyRunTest;

public class DepthFirstTerminateTest extends ModelCheckerTestCase {

	public DepthFirstTerminateTest() {
		super("DepthFirstTerminate", "", new String[] { "-dfid", "50" });
	}

	@Category(IndependentlyRunTest.class)
	@Ignore // Not valid due to (https://github.com/tlaplus/tlaplus/issues/548)
	@Test
	public void testSpec() {
		assertTrue(recorder.recorded(EC.TLC_FINISHED));
		assertFalse(recorder.recorded(EC.GENERAL));
	}

	/* (non-Javadoc)
	 * @see tlc2.tool.liveness.ModelCheckerTestCase#getNumberOfThreads()
	 */
	@Override
	protected int getNumberOfThreads() {
		// Run this test with as many threads possible to hopefully spot concurrency issues.
		return Runtime.getRuntime().availableProcessors();
	}
}