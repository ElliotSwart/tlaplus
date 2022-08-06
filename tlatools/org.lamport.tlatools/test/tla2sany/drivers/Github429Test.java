package tla2sany.drivers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.experimental.categories.Category;
import tla2sany.modanalyzer.SpecObj;
import tlc2.tool.CommonTestCase;
import util.IndependentlyRunTest;
import util.SimpleFilenameToStream;
import util.ToolIO;

public class Github429Test {

	private SpecObj moduleSpec;
	private SANY sany;
	@Before
	public void setUp() throws Exception {
		// create a model and initialize
		moduleSpec = new SpecObj(CommonTestCase.BASE_PATH + "Github429.tla", new SimpleFilenameToStream());
		sany = new SANY();
		sany.frontEndInitialize(moduleSpec, ToolIO.out);
	}

	@Category(IndependentlyRunTest.class)
	@Test
	public void testForFailedParse() {
        try {
			sany.frontEndParse(moduleSpec, ToolIO.out);
			sany.frontEndSemanticAnalysis(moduleSpec, ToolIO.out, false);
		} catch (final Exception e) {
			Assert.fail("No exception should occur during parse. Instead encountered [" + e.getClass()
								+ "] with message: " + e.getMessage());
		}
	}
}
