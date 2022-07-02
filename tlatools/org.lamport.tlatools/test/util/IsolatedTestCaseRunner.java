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
package util;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath ;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

public class IsolatedTestCaseRunner extends Runner {

	private Object adapter;

	public IsolatedTestCaseRunner(final Class<?> testFileClass) 
			throws InitializationError {
	
		System.gc();

		try {
			var classLoader = IsolatedClassLoader.getClassLoader();

			var Adapter = classLoader.loadClass("util.JUnitAdapter");

			Class[] cArg = new Class[1];
			cArg[0] = String.class;
			Object[] arg = new Object[1];
			arg[0] = testFileClass.getName();
			adapter = Adapter.getDeclaredConstructor(cArg).newInstance(arg);

			//delegate = new JUnit4(testClass);
		} catch (Exception e) {
			throw new InitializationError("Error during initialization");
		}
	}

	@Override
	public Description getDescription() {
		try {
			Method getDescriptionMethod = this.adapter.getClass().getMethod("getDescription");
			var description = (Description) getDescriptionMethod.invoke(adapter);
			return description;
		} catch (Exception e) {
			return Description.createTestDescription("fail", "fail", "fail");
		}
	}

	@Override
	public void run(final RunNotifier notifier) {
		try {
			Method runMethod = this.adapter.getClass().getMethod("run", RunNotifier.class);
			runMethod.invoke(adapter, notifier);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
