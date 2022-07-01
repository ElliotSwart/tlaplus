// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Wed 12 Jul 2017 at 16:10:00 PST by ian morris nieves
//      modified on Mon 30 Apr 2007 at 13:21:01 PST by lamport
//      modified on Sat Nov 13 12:43:44 PST 1999 by yuanyu

package tlc2.value.impl;

import tlc2.tool.FingerprintException;
import tlc2.util.Vect;
import tlc2.value.IValue;
import tlc2.value.Values;
import util.Assert;
import util.WrongInvocationException;

public class OpRcdValue extends OpValue implements Applicable {


  private static final long serialVersionUID = 6268427410634909269L;

  public final Vect<Value[]> domain;
  public final Vect<Value> values;

  /* Constructor */
  public OpRcdValue() {
    this.domain = new Vect<>();
    this.values = new Vect<>();
  }

  public OpRcdValue(final Vect<Value[]> domain, final Vect<Value> values) {
    this.domain = domain;
    this.values = values;
  }

  @Override
  public final byte getKind() { return OPRCDVALUE; }

  @Override
  public final int compareTo(final Object obj) {
    try {
      Assert.fail("Attempted to compare operator " + Values.ppr(this.toString()) +
      " with value:\n" + Values.ppr(obj.toString()), getSource());
      return 0;         // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  public final boolean equals(final Object obj) {
    try {
      Assert.fail("Attempted to check equality of operator " + Values.ppr(this.toString()) +
      " with value:\n" + Values.ppr(obj.toString()), getSource());
      return false;     // make compiler happy
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
      "\nis an element of operator " + Values.ppr(this.toString()), getSource());
      return false;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final boolean isFinite() {
    try {
      Assert.fail("Attempted to check if the operator " + Values.ppr(this.toString()) +
      " is a finite set.", getSource());
      return false;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  public final void addLine(final Vect<Value> vs) {
    try {
      final int len = vs.size();
      final Value[] args = new Value[len-2];
      for (int i = 0; i < len-2; i++) {
        args[i] = vs.elementAt(i+1);
      }
      this.domain.addElement(args);
      this.values.addElement(vs.elementAt(len-1));
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value apply(final Value arg, final int control) {
    try {
      throw new WrongInvocationException("Should use the other apply method.");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value apply(final Value[] args, final int control) {
    try {
      final int sz = this.domain.size();
      for (int i = 0; i < sz; i++) {
        final Value[] vals = this.domain.elementAt(i);
        if (args.length != vals.length) {
          Assert.fail("Attempted to apply the operator " + Values.ppr(this.toString()) +
          "\nwith wrong number of arguments.", getSource());
        }
        boolean matched = true;
        for (int j = 0; j < vals.length; j++) {
          matched = vals[j].equals(args[j]);
          if (!matched) break;
        }
        if (matched) {
          return this.values.elementAt(i);
        }
      }
      // Generate the error message:
      final StringBuilder msg = new StringBuilder("Attempted to apply operator:\n" + Values.ppr(this.toString()) +
              "\nto arguments (");
      if (args.length > 0) msg.append(args[0]);
      for (int i = 1; i < args.length; i++) {
        msg.append(", ").append(args[i]);
      }
      Assert.fail(msg +  "), which is undefined.", getSource());
      return null;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value select(final Value arg) {
    try {
      Assert.fail("Attempted to call OpRcdValue.select(). This is a TLC bug.", getSource());
      return null;   // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value takeExcept(final ValueExcept ex) {
    try {
      Assert.fail("Attempted to appy EXCEPT construct to the operator " +
      Values.ppr(this.toString()) + ".", getSource());
      return null;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value takeExcept(final ValueExcept[] exs) {
    try {
      Assert.fail("Attempted to apply EXCEPT construct to the operator " +
      Values.ppr(this.toString()) + ".", getSource());
      return null;     // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value getDomain() {
    try {
      Assert.fail("Attempted to compute the domain of the operator " +
      Values.ppr(this.toString()) + ".", getSource());
      return SetEnumValue.EmptySet;   // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final int size() {
    try {
      Assert.fail("Attempted to compute the number of elements in the operator " +
      Values.ppr(this.toString()) + ".", getSource());
      return 0;         // make compiler happy
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  /* Should never normalize an operator. */
  @Override
  public final boolean isNormalized() {
    try {
      throw new WrongInvocationException("Should not normalize an operator.");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final Value normalize() {
    try {
      throw new WrongInvocationException("Should not normalize an operator.");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final boolean isDefined() {
    try {
      boolean defined = true;
      for (int i = 0; i < this.values.size(); i++) {
        defined = defined && this.values.elementAt(i).isDefined();
      }
      return defined;
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  @Override
  public final IValue deepCopy() { return this; }

  @Override
  public final boolean assignable(final Value val) {
    try {
      throw new WrongInvocationException("Should not initialize an operator.");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

  /* Pretty-printing  */
  @Override
  public final StringBuffer toString(StringBuffer sb, final int offset, final boolean swallow) {
    try {
      sb.append("{ ");
      if (this.values.size() != 0) {
        sb.append("<");
        final Value[] args = this.domain.elementAt(0);
          for (final Value arg : args) {
              sb = arg.toString(sb, offset, swallow);
              sb.append(", ");
          }
        sb = this.values.elementAt(0).toString(sb, offset, swallow);
        sb.append(">");
      }
      for (int i = 1; i < this.values.size(); i++) {
        sb.append(", <");
        final Value[] args = this.domain.elementAt(i);
          for (final Value arg : args) {
              sb = arg.toString(sb, offset, swallow);
              sb.append(", ");
          }
        sb = this.values.elementAt(i).toString(sb, offset, swallow);
        sb.append(">");
      }
      return sb.append("}");
    }
    catch (final RuntimeException | OutOfMemoryError e) {
      if (hasSource()) { throw FingerprintException.getNewHead(this, e); }
      else { throw e; }
    }
  }

}
