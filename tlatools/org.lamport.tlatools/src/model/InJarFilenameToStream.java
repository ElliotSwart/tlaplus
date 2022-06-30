package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import util.FilenameToStream;
import util.SimpleFilenameToStream;

public class InJarFilenameToStream extends SimpleFilenameToStream implements
		FilenameToStream {
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	private final String prefix;

	public InJarFilenameToStream(final String prefix) {
		this.prefix = prefix;
	}

	@Override
	public File resolve(final String name, final boolean isModule) {
		final InputStream is = InJarFilenameToStream.class.getResourceAsStream(prefix + name);
		if(is != null) {
			try {
				final File sourceFile = new TLAFile(TMPDIR + File.separator + name, InJarFilenameToStream.class.getResource(prefix + name), false, this);
				sourceFile.deleteOnExit();
				
				final FileOutputStream fos = new FileOutputStream(sourceFile);
				
				final byte[] buf = new byte[1024];
				int len;
				while ((len = is.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fos.close();
				is.close();
				return sourceFile;
			} catch (final IOException e) {
				// must not happen
				e.printStackTrace();
			}
		}
		return super.resolve(name, isModule);
	}

	@Override
	public File resolve(final String name) {
		return super.resolve(name);
	}
}
