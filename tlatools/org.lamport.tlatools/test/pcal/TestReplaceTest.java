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
package pcal;

import org.junit.Test;
import tlc2.output.EC;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestReplaceTest extends PCalModelCheckerTestCase {

    public TestReplaceTest() {
        super("TestReplace", "pcal");
    }

    @Test
    public void testSpec() {
        assertTrue(recorder.recordedWithStringValue(EC.TLC_INIT_GENERATED1, "1"));
        assertTrue(recorder.recorded(EC.TLC_FINISHED));
        assertFalse(recorder.recorded(EC.GENERAL));
        assertTrue(recorder.recordedWithStringValues(EC.TLC_STATS, "18", "17", "0"));
        assertTrue(recorder.recordedWithStringValue(EC.TLC_SEARCH_DEPTH, "17"));

        assertUncovered("line 124, col 32 to line 124, col 41 of module TestReplace: 0\n"
                + "line 175, col 33 to line 175, col 41 of module TestReplace: 0");
    }
}
