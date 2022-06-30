// Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:15:53 PST by lamport
//      modified on Tue May 15 11:44:57 PDT 2001 by yuanyu

package tlc2.tool.fp.dfid;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * An <code>MultiFPInt</code> is a set of 64-bit fingerprints.
 */
public class MultiFPIntSet extends FPIntSet {

  private final FPIntSet[] sets;
  private final int fpbits;

  public MultiFPIntSet(final int bits) throws RemoteException {
    final int len = 1 << bits;
    this.sets = new FPIntSet[len];
    for (int i = 0; i < len; i++) {
      this.sets[i] = new MemFPIntSet();
    }
    this.fpbits = 64 - bits;
  }

  @Override
  public final void init(final int numThreads, final String metadir, final String filename)
  throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].init(numThreads, metadir, filename+"_"+i);
    }
  }
  
  /**
   * Returns the number of fingerprints in this set.
   * Warning: The size is only accurate in single-threaded mode.
   */
  @Override
  public final long size() {
    int sum = 0;
    for (int i = 0; i < this.sets.length; i++) {
      sum += this.sets[i].size();
    }
    return sum;
  }

  @Override
  public final void setLeveled(final long fp) {
    final int idx = (int)(fp >>> this.fpbits);
    this.sets[idx].setLeveled(fp);
  }
  
  @Override
  public final int setStatus(final long fp, final int status) {
    final int idx = (int)(fp >>> this.fpbits);
    return this.sets[idx].setStatus(fp, status);
  }

  /* Returns the status of fp. */
  @Override
  public final int getStatus(final long fp) {
    final int idx = (int)(fp >>> this.fpbits);
    return this.sets[idx].getStatus(fp);
  }

  @Override
  public final boolean allLeveled() {
    for (int i = 0; i < this.sets.length; i++) {
      if (!this.sets[i].allLeveled()) return false;
    }
    return true;
  }

  @Override
  public final void close() {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].close();
    }
  }

  /* This is not quite correct. */
  @Override
  public final long checkFPs() throws IOException {
    long res = Long.MIN_VALUE;
    for (int i = 0; i < this.sets.length; i++) {
      res = Math.max(res, this.sets[i].checkFPs());
    }
    return res;
  }

  @Override
  public final void exit(final boolean cleanup) throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].exit(cleanup);
    }
  }
  
  @Override
  public final void beginChkpt() throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].beginChkpt();
    }
  }
  
  @Override
  public final void commitChkpt() throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].commitChkpt();
    }
  }
       
  @Override
  public final void recover() throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].recover();
    }
  }

  @Override
  public final void beginChkpt(final String filename) throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].beginChkpt(filename);
    }
  }
  
  @Override
  public final void commitChkpt(final String filename) throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].commitChkpt(filename);
    }
  }
  
  @Override
  public final void recover(final String filename) throws IOException {
    for (int i = 0; i < this.sets.length; i++) {
      this.sets[i].recover(filename);
    }
  }

}
