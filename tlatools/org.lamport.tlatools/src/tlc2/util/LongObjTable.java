// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:32 PST by lamport
//      modified on Mon Nov 26 15:41:48 PST 2001 by yuanyu

package tlc2.util;

public final class LongObjTable {
  private int count;
  private int length;
  private int thresh;
  private long[] keys;
  private Object[] elems;

  public LongObjTable(final int size) {
    this.keys = new long[size];
    this.elems = new Object[size];
    this.count = 0;
    this.length = size;
    this.thresh = this.length / 2;
  }

  private void grow() {
    final long[] oldKeys = this.keys;
    final Object[] oldElems = this.elems;
    this.count = 0;
    this.length = 2 * this.length + 1;
    this.thresh = this.length / 2;
    this.keys = new long[length];
    this.elems = new Object[length];
    for (int i = 0; i < oldKeys.length; i++) {
      final Object elem = oldElems[i];
      if (elem != null) this.put(oldKeys[i], elem);
    }
  }

  public int size() { return this.count; }

  public int put(final long k, final Object elem) {
    if (count >= thresh) this.grow();
    int loc = ((int)k & 0x7FFFFFFF) % length ;
    while (true) {
      if (this.elems[loc] == null) {
	this.keys[loc] = k;
	this.elems[loc] = elem;
	this.count++;
	return loc;
      }
      else if (this.keys[loc] == k) {
	this.elems[loc] = elem;
	return loc;
      }
      loc = (loc + 1) % length;
    }
  }

  public Object get(final long k) {
    int loc = ((int)k & 0x7FFFFFFF) % length ;
    while (true) {
      final Object elem = this.elems[loc];
      if (elem == null) return null;
      if (this.keys[loc] == k) return elem;
      loc = (loc + 1) % length;
    }
  }

}
