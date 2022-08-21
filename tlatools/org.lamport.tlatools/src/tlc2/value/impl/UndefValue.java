// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Wed 12 Jul 2017 at 16:10:00 PST by ian morris nieves
//      modified on Mon 30 Apr 2007 at 13:21:06 PST by lamport
//      modified on Tue Aug 15 23:08:23 PDT 2000 by yuanyu

package tlc2.value.impl;

import tlc2.tool.FingerprintException;
import tlc2.value.IValue;
import tlc2.value.Values;
import util.Assert;

public class UndefValue extends Value {

  private static final long serialVersionUID = -2146173262270469092L;
public static final UndefValue ValUndef = new UndefValue();

  public UndefValue() { /*SKIP*/ }

  @Override
  public byte getKind() { return UNDEFVALUE; }

  @Override
  public final int compareTo(final Object obj) {
    try {
      return (obj instanceof UndefValue) ? 0 : 1;
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  public final boolean equals(final Object obj) {
    try {
      return (obj instanceof UndefValue);
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final boolean member(final Value elem) {
    try {
      Assert.fail("Attempted to check if the value:\n" + Values.ppr(elem.toString()) +
      "\nis an element " + Values.ppr(this.toString()), getSource());
      return false;    // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final boolean isFinite() {
    try {
      Assert.fail("Attempted to check if the value " + Values.ppr(this.toString()) +
      " is a finite set.", getSource());
      return false;    // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value takeExcept(final ValueExcept ex) {
    try {
      if (ex.idx < ex.path.length) {
        Assert.fail("Attempted to apply EXCEPT construct to the value " +
        Values.ppr(this.toString()) + ".", getSource());
      }
      return ex.value;
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value takeExcept(final ValueExcept[] exs) {
    try {
      if (exs.length != 0) {
        Assert.fail("Attempted to apply EXCEPT construct to the value " +
        Values.ppr(this.toString()) + ".", getSource());
      }
      return this;
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final int size() {
    try {
      Assert.fail("Attempted to compute the number of elements in the value " +
      Values.ppr(this.toString()) + ".", getSource());
      return 0;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public boolean mutates() {
	  return false;
  }

  @Override
  public final boolean isNormalized() { return true; }

  @Override
  public final Value normalize() { /*nop*/return this; }

  @Override
  public final boolean isDefined() { return false; }

  @Override
  public final IValue deepCopy() { return this; }

  @Override
  public final boolean assignable(final Value val) { return true; }

  /* The string representation. */
  @Override
  public final StringBuilder toString(final StringBuilder sb, final int offset, final boolean swallow) {
    try {
      return sb.append("UNDEF");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

}
