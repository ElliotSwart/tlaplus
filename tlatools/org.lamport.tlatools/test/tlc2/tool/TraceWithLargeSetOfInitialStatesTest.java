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

import org.junit.Test;
import tlc2.output.EC;
import tlc2.output.EC.ExitStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TraceWithLargeSetOfInitialStatesTest extends ModelCheckerTestCase {

    public TraceWithLargeSetOfInitialStatesTest() {
        super("TraceWithLargeSetOfInitialStatesTest", new String[]{"-maxSetSize", "10"}, ExitStatus.VIOLATION_SAFETY);
    }

    @Test
    public void testSpec() {
        assertTrue(recorder.recorded(EC.TLC_FINISHED));
        assertFalse(recorder.recorded(EC.GENERAL));
        assertFalse(recorder.recorded(EC.TLC_BUG));

        assertTrue(recorder.recorded(EC.TLC_BEHAVIOR_UP_TO_THIS_POINT));

        final List<String> expectedTrace = new ArrayList<>(2);
        expectedTrace.add("/\\ x = 1\n/\\ y = FALSE");
        expectedTrace.add("/\\ x = 1\n/\\ y = TRUE");
        final List<String> expectedActions = new ArrayList<>(2);
        expectedActions.add(isExtendedTLCState()
                ? "<Initial predicate line 6, col 25 to line 6, col 33 of module TraceWithLargeSetOfInitialStatesTest>"
                : TLCStateInfo.INITIAL_PREDICATE);
        expectedActions.add("<Action line 6, col 42 to line 6, col 60 of module TraceWithLargeSetOfInitialStatesTest>");
        assertTraceWith(recorder.getRecords(EC.TLC_STATE_PRINT2), expectedTrace, expectedActions);

        assertZeroUncovered();
    }
}
