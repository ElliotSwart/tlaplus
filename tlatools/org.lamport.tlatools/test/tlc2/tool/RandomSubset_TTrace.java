/*******************************************************************************
 * Copyright (c) 2018 Microsoft Research. All rights reserved. 
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
import org.junit.experimental.categories.Category;
import tlc2.output.EC;
import tlc2.output.EC.ExitStatus;
import tlc2.tool.liveness.TTraceModelCheckerTestCase;
import util.TTraceTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public abstract class RandomSubset_TTrace extends TTraceModelCheckerTestCase {

    private final int x;
    private final int y;

    public RandomSubset_TTrace(final Class<?> clazz, final int x, final int y) {
        super(clazz, ExitStatus.VIOLATION_SAFETY);
        this.x = x;
        this.y = y;
    }

    @Category(TTraceTest.class)
    @Test
    public void testSpec() {
        assertTrue(recorder.recorded(EC.TLC_FINISHED));
        assertFalse(recorder.recorded(EC.GENERAL));

        assertTrue(recorder.recordedWithStringValue(EC.TLC_INIT_GENERATED1, "1"));
        assertTrue(recorder.recordedWithStringValues(EC.TLC_STATS, "2", "2", "0"));
        assertEquals(2, recorder.getRecordAsInt(EC.TLC_SEARCH_DEPTH));

        assertTrue(recorder.recorded(EC.TLC_STATE_PRINT2));
        final List<String> expectedTrace = new ArrayList<>();
        expectedTrace.add("/\\ x = " + x + "\n" + "/\\ y = " + y + "\n" + "/\\ z = TRUE");
        expectedTrace.add("/\\ x = " + x + "\n" + "/\\ y = " + y + "\n" + "/\\ z = FALSE");
        assertTraceWith(recorder.getRecords(EC.TLC_STATE_PRINT2), expectedTrace);
    }

}