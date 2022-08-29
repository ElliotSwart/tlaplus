/*******************************************************************************
 * Copyright (c) 2021 Microsoft Research. All rights reserved. 
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

package tlc2.tool.simulation;

import org.junit.Test;
import tlc2.TLC;
import tlc2.output.EC;
import tlc2.tool.ModelCheckerTestCase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Github602Test extends ModelCheckerTestCase {

    public Github602Test() {
        super("Github602", new String[]{"-config", "Github602.tla", "-simulate"});
        TLC.setTraceNum(1);
    }

    @Test
    public void testSpec() {
        // Simulation has finished and generated states
        assertTrue(recorder.recorded(EC.TLC_FINISHED));
        assertFalse(recorder.recorded(EC.GENERAL));
        assertTrue(recorder.recordedWithStringValues(EC.TLC_PROGRESS_SIMU, "156", "1", "100", "0", "0"));
    }
}
