// Copyright (c) 2003 Compaq Corporation.  All rights reserved.

package tla2sany.utilities;

public class IntWrapper {
  private int i;
  public IntWrapper() { i = 0 ; }
  public IntWrapper(final int initial ) { i = initial ; }

  public final void inc() { i++; }
  public final void inc(final int increment ) { i+=increment; }

  public final int value() { return i; }

  public final void set(final int value ) { i = value; }
}
