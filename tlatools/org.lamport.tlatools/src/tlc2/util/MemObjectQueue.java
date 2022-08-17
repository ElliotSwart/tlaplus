// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:34 PST by lamport
//      modified on Mon Dec 18 22:56:08 PST 2000 by yuanyu

package tlc2.util;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tlc2.output.EC;
import util.Assert;
import util.FileUtil;

/**
 * 
 * @version $Id$
 */
public final class MemObjectQueue {
  private final static int InitialSize = 4096;
  private final static int GrowthFactor = 2;

  /* Fields  */
  private int len;
  private Object[] states;
  private int start = 0;
  private final String diskdir;
    
  public MemObjectQueue(final String metadir) {
    this.states = new Object[InitialSize];
    this.start = 0;
    this.diskdir = metadir;
  }
    
  public void enqueue(final Object state) {
    if (this.len == this.states.length) {
      // grow the array
      final int newLen = Math.max(1, this.len * GrowthFactor);
      final Object[] newStates = new Object[newLen];
      final int copyLen = this.states.length - this.start;
      System.arraycopy(this.states, this.start, newStates, 0, copyLen);
      System.arraycopy(this.states, 0, newStates, copyLen, this.start);
      this.states = newStates;
      this.start = 0;
    }
    final int last = (this.start + this.len) % this.states.length;
    this.states[last] = state;
    this.len++;
  }
    
  public Object dequeue() {
    if (this.len == 0) return null;
    final Object res = this.states[this.start];
    this.states[this.start] = null;
    this.start = (this.start + 1) % this.states.length;
    this.len--;
    return res;
  }

  // Checkpoint.
  public void beginChkpt() throws IOException {
    final String filename = this.diskdir + FileUtil.separator + "queue.tmp";
    try(final ObjectOutputStream oos = FileUtil.newOBFOS(filename)){
      oos.writeInt(this.len);
      int index = this.start;
      for (int i = 0; i < this.len; i++) {
        oos.writeObject(this.states[index++]);
        if (index == this.states.length) index = 0;
      }
    }
  }

  public void commitChkpt() throws IOException {
    final String oldName = this.diskdir + FileUtil.separator + "queue.chkpt";
    final File oldChkpt = new File(oldName);
    final String newName = this.diskdir + FileUtil.separator + "queue.tmp";
    final File newChkpt = new File(newName);
    if ((oldChkpt.exists() && !oldChkpt.delete()) ||
	!newChkpt.renameTo(oldChkpt)) {
      throw new IOException("MemStateQueue.commitChkpt: cannot delete " + oldChkpt);
    }
  }
  
  public void recover() throws IOException {
    final String filename = this.diskdir + FileUtil.separator + "queue.chkpt";
    try(final ObjectInputStream ois = FileUtil.newOBFIS(filename)){
      this.len = ois.readInt();

      for (int i = 0; i < this.len; i++) {
        this.states[i] = ois.readObject();
      }
    }
    catch (final ClassNotFoundException e) {
      Assert.fail(EC.SYSTEM_CHECKPOINT_RECOVERY_CORRUPT, e.getMessage());
    }
  }
}
