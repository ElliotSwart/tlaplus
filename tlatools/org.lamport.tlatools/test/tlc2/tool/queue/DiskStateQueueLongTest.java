package tlc2.tool.queue;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import tla2sany.semantic.OpDeclNode;
import tlc2.tool.ITool;
import tlc2.tool.TLCState;
import tlc2.tool.TLCStateMut;
import tlc2.value.IMVPerm;
import tlc2.value.IValue;
import util.LongTest;

public class DiskStateQueueLongTest extends StateQueueTest {

	private File file;

	/* (non-Javadoc)
	 * @see tlc2.tool.queue.StateQueueTest#setUp()
	 */
	@Override
    @Before
	public void setUp() throws Exception {
		// create a temp folder in java.io.tmpdir and have it deleted on VM exit
		final String diskdir = System.getProperty("java.io.tmpdir") + File.separator + "MultiDiskStateQueueTest_"
				+ System.currentTimeMillis();
		file = new File(diskdir);
		file.mkdirs();
		file.deleteOnExit();

		var tool = EasyMock.createNiceMock(ITool.class);

		var empty = new TLCStateMut(new OpDeclNode[]{}, new IValue[]{}, tool, null, new IMVPerm[]{});

		sQueue = new DiskStateQueue(diskdir, empty);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After
	public void tearDown() {
		// delete all nested files
		final File[] listFiles = file.listFiles();
        for (final File aFile : listFiles) {
            aFile.delete();
        }
		file.delete();
	}
	
	// add Integer.MAX_VALUE states and check growth of MultiStateQueue. 
	// Reuse the same state to speed up instantiation and space requirements
	@Category(LongTest.class)
	@Test
	public void testGrowBeyondIntMaxValue() {
		final TLCState state = new DummyTLCState(new OpDeclNode[]{});

		final long j = Integer.MAX_VALUE + 1L;
		for (long i = 0; i < j; i++) {
			sQueue.sEnqueue(state);
		}
		assertTrue(sQueue.size() == j);
	}
}
