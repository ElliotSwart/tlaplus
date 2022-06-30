// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:18:27 PST by lamport  
//      modified on Thu Feb  8 23:31:49 PST 2001 by yuanyu   

package tlc2.util;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tlc2.output.EC;
import tlc2.output.MP;
import tlc2.tool.TLCState;
import tlc2.value.ValueOutputStream;
import util.Assert;

public class StatePoolWriter extends Thread {

    private TLCState[] buf;     
    private File poolFile;           // the file to be written
    private final StatePoolReader reader;  // the consumer if not null

    
  public StatePoolWriter(final int bufSize) {
	  this(bufSize, null);
  }

  public StatePoolWriter(final int bufSize, final StatePoolReader reader) {
	  super("TLCStatePoolWriter");
    this.buf = new TLCState[bufSize];
    this.poolFile = null;
    this.reader = reader;
  }

  /*
   * This method first completes the preceding write if not started.
   * It then notifies this writer to flush enqBuf to file. In practice,
   * we expect the preceding write to have been completed. 
   */
  public final synchronized TLCState[] doWork(final TLCState[] enqBuf, final File file)
  throws IOException {
    if (this.poolFile != null) {
      final ValueOutputStream vos = new ValueOutputStream(this.poolFile);
        for (TLCState tlcState : this.buf) {
            tlcState.write(vos);
        }
      vos.close();
    }
    final TLCState[] res = this.buf;
    this.buf = enqBuf;
    this.poolFile = file;
    this.notify();
    return res;
  }

  /* Spin waiting for the write to complete.  */
  public final void ensureWritten() throws InterruptedException {
    synchronized(this) {
      while (this.poolFile != null) {
	this.wait();
      }
    }
  }

  public final synchronized void beginChkpt(final ObjectOutputStream oos)
  throws IOException {
    final boolean hasFile = (this.poolFile == null) ? false : true;
    oos.writeBoolean(hasFile);
    if (hasFile) {
      oos.writeObject(this.poolFile);
        for (TLCState tlcState : this.buf) {
            oos.writeObject(tlcState);
        }
    }
  }

  /* Note this method is not synchronized.  */
  public final void recover(final ObjectInputStream ois) throws IOException {
    final boolean hasFile = ois.readBoolean();
    if (hasFile) {
      try {
	this.poolFile = (File)ois.readObject();
	for (int i = 0; i < this.buf.length; i++) {
	  this.buf[i] = (TLCState)ois.readObject();
	}
      }
      catch (final ClassNotFoundException e)
      {
          Assert.fail(EC.SYSTEM_CHECKPOINT_RECOVERY_CORRUPT, e);
      }
    }
    else {
      this.poolFile = null;
    }
  }

  /**
   * Write "buf" to "poolFile". The objects in the queue are written
   * using Java's object serialization facilities.
   */
  @Override
  public void run() {
    try {
      synchronized(this) {
	while (true) {
	  while (this.poolFile == null) {
	    this.wait();
	    // we are done without ever receiving a pool file
	    if(this.poolFile == null) {
	    	return;
	    }
	  }
	  final ValueOutputStream vos = new ValueOutputStream(this.poolFile);
        for (TLCState tlcState : this.buf) {
            tlcState.write(vos);
        }
	  vos.close();
	  this.poolFile = null;
	  this.notify();
	  if (this.reader != null) this.reader.wakeup();
	}
      }
    }
    catch (final Exception e) {
      // Assert.printStack(e);
        MP.printError(EC.SYSTEM_ERROR_WRITING_POOL, e.getMessage(), e);
      System.exit(1);
    }
  }
  
}
