// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 15:29:55 PST by lamport
//      modified on Tue Nov  9 11:06:41 PST 1999 by yuanyu

package tlc2.tool.impl;

import tla2sany.semantic.SymbolNode;
import tlc2.output.EC;
import tlc2.tool.IContextEnumerator;
import tlc2.util.Context;
import tlc2.value.impl.TupleValue;
import tlc2.value.impl.Value;
import tlc2.value.impl.ValueEnumeration;
import util.Assert;

public final class ContextEnumerator implements IContextEnumerator {
  private final Context con;
  private final Object[] vars;
  private final ValueEnumeration[] enums;
  private final Value[] currentElems;
  private boolean isDone;
  
  public ContextEnumerator(final Object[] vars, final ValueEnumeration[] enums, final Context con) {
    this.con = con;
    this.vars = vars;
    this.enums = enums;
    this.currentElems = new Value[enums.length];
    this.isDone = false;
    for (int i = 0; i < enums.length; i++) {
      this.currentElems[i] = this.enums[i].nextElement();
      if (this.currentElems[i] == null) {
	this.isDone = true;
	break;
      }
    }
  }
  
  @Override
  public Context nextElement() {
      Context con1 = this.con;
      if (this.isDone) return null;
      for (int i = 0; i < enums.length; i++) {
          if (this.vars[i] instanceof SymbolNode symNode) {
              con1 = con1.cons(symNode, this.currentElems[i]);
          }
          else {
              final SymbolNode[] varList = (SymbolNode[])this.vars[i];
              final Value argVal = this.currentElems[i];
              if (!(argVal instanceof TupleValue)) {
                  Assert.fail(EC.TLC_ARGUMENT_MISMATCH, varList[0].toString());
              }
              final Value[] valList = ((TupleValue)argVal).elems;
              if (varList.length != valList.length) {
                  Assert.fail(EC.TLC_ARGUMENT_MISMATCH, varList[0].toString());
              }
              for (int j = 0; j < varList.length; j++) {
                  con1 = con1.cons(varList[j], valList[j]);
              }
          }
      }
      for (int i = 0; i < enums.length; i++) {
          this.currentElems[i] = this.enums[i].nextElement();
          if (this.currentElems[i] != null) break;
          if (i == this.enums.length - 1) {
              this.isDone = true;
              break;
          }
          this.enums[i].reset();
          this.currentElems[i] = this.enums[i].nextElement();
      }
      return con1;
  }

  public boolean isDone() {
	return isDone;
  }
}

