// Copyright (c) 2011 Microsoft Corporation.  All rights reserved.
package tlc2.tool.fp;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class Bug210DiskFPSetTest extends AbstractFPSetTest {

    /* (non-Javadoc)
     * @see tlc2.tool.fp.AbstractFPSetTest#getFPSet(int)
     */
    @Override
    protected FPSet getFPSet(final FPSetConfiguration fpSetConfig) throws IOException {
        final DummyDiskFPSet fpSet = new DummyDiskFPSet(fpSetConfig);
        fpSet.init(1, tmpdir, filename);
        return fpSet;
    }

    /**
     * @see <a href="../../general/bugzilla/index.html">Bug #210</a>
     */
    @Test
    public void testDiskLookupWithOverflow() throws Exception {
        // set up an index whose upper bound is beyond 1/1024 of
        // Integer.MAX_VALUE
        //
        // (this calculation is executed in diskLookup to map from in-memory
        // index addresses to on-disk addresses)
        final int size = (Integer.MAX_VALUE / DiskFPSet.NumEntriesPerPage) + 8;
        final long[] anIndex = new long[size];
        anIndex[size - 2] = Long.MAX_VALUE - 3;
        anIndex[size - 1] = Long.MAX_VALUE - 1;

        final DummyDiskFPSet fpSet = (DummyDiskFPSet) getFPSet(new FPSetConfiguration());

        // do a diskLookup for a non-existent fp that accesses the index values
        // [size - 2, b = size - 1]. These two are "close" to an int overflow if
        // multiplied by 2^10 (DiskFPSet#NumEntriesPerPage).
        try (fpSet) {
            fpSet.setIndex(anIndex);
            assertFalse(fpSet.diskLookup(Long.MAX_VALUE - 2));
        } catch (final IOException e) {
            fail(e.getMessage());
        }
    }
}
