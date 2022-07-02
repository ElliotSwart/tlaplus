package util;


import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class JUnitAdapter {
    private BlockJUnit4ClassRunner runner;

    public JUnitAdapter(String className) throws ClassNotFoundException, InitializationError {
        var testClass = this.getClass().getClassLoader().loadClass(className);
        runner = new BlockJUnit4ClassRunner(testClass);

        
    }

    public void run(final RunNotifier notifier) {
        var l = runner.getClass().getClassLoader();
        runner.run(notifier);
    }

    public Description getDescription() {
		return runner.getDescription();
	}
}