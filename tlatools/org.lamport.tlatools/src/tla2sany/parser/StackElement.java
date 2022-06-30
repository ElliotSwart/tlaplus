// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.parser;

class StackElement {
  int Kind;
  int Offset;

  StackElement(final int o, final int k) { Kind = k; Offset = o; }
}
