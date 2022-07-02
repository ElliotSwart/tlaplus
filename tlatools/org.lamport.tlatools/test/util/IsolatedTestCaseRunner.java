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

	private final JUnit4 delegate;

	public static ImmutableMap<String, ClassInfo> classPaths;

	public static ImmutableMap<String, ClassInfo> getClassPaths() throws IOException {
		
		if (classPaths == null){
			var classes = ClassPath.from(
								ClassLoader.getSystemClassLoader())
								.getTopLevelClasses();
			
			var b = new ImmutableMap.Builder<String, ClassInfo>();

			for(var c : classes){
				b.put(c.getName(), c);
			}

			IsolatedTestCaseRunner.classPaths = b.build();
		}

		return IsolatedTestCaseRunner.classPaths;
	}

	public IsolatedTestCaseRunner(final Class<?> testFileClass) 
			throws InitializationError, ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
	
		// Since IsolatedTestCaseRunner runs several isolated tests in a single VM, it
		// is good practice to clean resources before each new test.
		System.gc();
		var classpaths = getClassPaths();
		
		var isolatedTestCaseClassLoader = new IsolatedTestCaseClassLoader(classpaths, this.getClass().getClassLoader());
		var testClass = isolatedTestCaseClassLoader.loadClass(testFileClass.getName());

		delegate = new JUnit4(testClass);
	}

	@Override
	public Description getDescription() {
		return delegate.getDescription();
	}

	@Override
	public void run(final RunNotifier notifier) {
		delegate.run(notifier);
	}
	
	private class IsolatedTestCaseClassLoader extends ClassLoader {

		private final Map<String, Class<?>> cache = new HashMap<>();
		private final Set<String> packages = new HashSet<>();

		private final ImmutableMap<String, ClassInfo> classPaths;

		public IsolatedTestCaseClassLoader(ImmutableMap<String, ClassInfo> classPaths, ClassLoader parent) {
			super(parent);
			this.classPaths = classPaths;

			// All of TLC's java packages. 
			packages.add("tla2sany");
			packages.add("pcal");
			packages.add("util");
			packages.add("tla2tex");
			packages.add("tlc2");

		}


		@Override
		public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			Class<?> loadedClass = this.findLoadedClass(name);

			if(!Objects.isNull(loadedClass)){
				return loadedClass;
			}

			if (loadedClass == null && classPaths.containsKey(name)){
				var classInfo = classPaths.get(name);
				var byteSource = classInfo.asByteSource();
				byte[] bytes;
				try {
					bytes = byteSource.read();
				} catch (IOException e) {
					throw new ClassNotFoundException();
				}

				var c = defineClass(name, bytes, 0, bytes.length);

				if(resolve){
					resolveClass(c);
				}

				return c;
			}

			return super.loadClass(name, resolve);
		}
	}
}
