// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at  9:27:29 PST by lamport
//      modified on Wed Jan 10 00:11:43 PST 2001 by yuanyu

package tlc2.tool.other;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * This class is not used from anywhere from the project
 * it seems to be a helper utility used during the development 
 * @deprecated according to the paths it is not used (SZ February 19, 2009) 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FileClassLoader extends ClassLoader {
  /* Load a class from a file. */
  private String dir;

  public FileClassLoader(final String dir) {
    this.dir = dir;
    if (dir.length() != 0 &&
	!dir.endsWith(File.separator)) {
      this.dir += File.separator;
    }
  }
    
  private byte[] loadClassData(final String name) {
    byte[] bytes = null;
    final String fileName = name + ".class";
    try {
      final FileInputStream fis = new FileInputStream(this.dir + fileName);
      final int size = fis.available();
      bytes = new byte[size];
      fis.read(bytes);
      fis.close();
    }
    catch (final IOException e) { bytes = null; }
    return bytes;
  }

  @Override
  public synchronized Class loadClass(final String name, final boolean resolve) {
    Class c = null;
    final byte[] data = loadClassData(name);
    if (data != null) {
      c = defineClass(name, data, 0, data.length);
      if (resolve) resolveClass(c);
    }
    return c;
  }

  public static void main(final String[] argv) {
    final FileClassLoader fcl = new FileClassLoader("/udir/yuanyu/proj/tlc/module");
    try {
      final Class c = fcl.loadClass("Strings", true);  // must set CLASSPATH correctly
      System.err.println(c);
    }
    catch (final Exception e) {
      // Assert.printStack();
      System.err.println("Error: " + e.getMessage());
    }
  }

}


