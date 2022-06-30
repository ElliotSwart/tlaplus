// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:34 PST by lamport
//      modified on Wed Oct 24 11:56:57 PDT 2001 by yuanyu

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
public final class MemObjectStack extends ObjectStack {
  private final static int InitialSize = 4096;
  private final static int GrowthFactor = 2;

  /* Fields  */
  private Object[] states;
  private final String filename;
    
  public MemObjectStack(final String metadir, final String name) {
    this.states = new Object[InitialSize];
    this.filename = metadir +  FileUtil.separator + name;
  }
    
  @Override
  void enqueueInner(final Object state) {
    if (this.len == this.states.length) {
      // grow the array
      final int newLen = Math.max(1, this.len * GrowthFactor);
      final Object[] newStates = new Object[newLen];
      System.arraycopy(this.states, 0, newStates, 0, this.len);
      this.states = newStates;
    }
    this.states[this.len] = state;
  }
    
  @Override
  Object dequeueInner() {
    final int head = this.len - 1;
    final Object res = this.states[head];
    this.states[head] = null;
    return res;
  }

  // Checkpoint.
  @Override
  public void beginChkpt() throws IOException {
    final String tmpfile = this.filename + ".tmp";
    
    final ObjectOutputStream oos = FileUtil.newOBFOS(tmpfile);
    oos.writeInt(this.len);
    for (int i = 0; i < this.len; i++) {
      oos.writeObject(this.states[i++]);
    }
    oos.close();
    
  }

  @Override
  public void commitChkpt() throws IOException {
    final String oldName = this.filename + ".chkpt";
    final File oldChkpt = new File(oldName);
    final String newName = this.filename + ".tmp";
    final File newChkpt = new File(newName);
    if (!newChkpt.renameTo(oldChkpt)) {
      throw new IOException("MemObjectStack.commitChkpt: cannot delete " + oldChkpt);
    }
  }
  
  @Override
  public void recover() throws IOException {
    final String chkptfile = this.filename + ".chkpt";
    final ObjectInputStream ois = FileUtil.newOBFIS(chkptfile);
    try (ois) {
      this.len = ois.readInt();
      for (int i = 0; i < this.len; i++) {
        this.states[i] = ois.readObject();
      }
    } catch (final ClassNotFoundException e) {
      Assert.fail(EC.SYSTEM_CHECKPOINT_RECOVERY_CORRUPT, e.getMessage());
    }
  }

}
