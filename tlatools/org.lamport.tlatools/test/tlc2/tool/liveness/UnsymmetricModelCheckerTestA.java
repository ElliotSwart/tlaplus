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

import org.junit.Test;
import tlc2.output.EC;
import tlc2.output.EC.ExitStatus;
import tlc2.tool.TLCStateInfo;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnsymmetricModelCheckerTestA extends ModelCheckerTestCase {

    public UnsymmetricModelCheckerTestA() {
        super("UnsymmetricMCA", "symmetry", ExitStatus.VIOLATION_LIVENESS);
    }

    @Test
    public void testSpec() {
        // ModelChecker intends to check liveness
        assertTrue(recorder.recordedWithStringValues(EC.TLC_LIVE_IMPLIED, "1"));
        assertTrue(recorder.recordedWithStringValues(EC.TLC_INIT_GENERATED2, "2", "s", "1"));

        // ModelChecker has finished and generated the expected amount of states
        assertTrue(recorder.recorded(EC.TLC_FINISHED));
        assertFalse(recorder.recorded(EC.GENERAL));
        assertTrue(recorder.recordedWithStringValues(EC.TLC_STATS, "5", "2", "0"));

        // Assert it has found a temporal violation and a counter example
        assertTrue(recorder.recorded(EC.TLC_TEMPORAL_PROPERTY_VIOLATED));
        assertTrue(recorder.recorded(EC.TLC_COUNTER_EXAMPLE));

        assertTrue(recorder.recorded(EC.TLC_STATE_PRINT2));
        final List<String> expectedTrace = new ArrayList<>(2);
        expectedTrace.add("x = a");
        expectedTrace.add("x = 1");

        final List<String> expectedActions = new ArrayList<>();
        expectedActions.add(isExtendedTLCState()
                ? "<Init line 5, col 10 to line 5, col 16 of module Unsymmetric>"
                : TLCStateInfo.INITIAL_PREDICATE);
        expectedActions.add("<NextA line 17, col 13 to line 19, col 36 of module Unsymmetric>");

        assertTraceWith(recorder.getRecords(EC.TLC_STATE_PRINT2), expectedTrace, expectedActions);

        assertBackToState(1);

        assertUncovered("line 19, col 31 to line 19, col 36 of module Unsymmetric: 0");
    }
}
