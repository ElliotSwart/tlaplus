// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.parser;

final class OSelement {
  final Operator op;
  final SyntaxTreeNode  node;
  int kind;

  OSelement(final SyntaxTreeNode n ) { node = n; op = null; }
  OSelement(final SyntaxTreeNode n, final Operator o ) { node = n; op = o; }

  final Operator getOperator() { return op; }
  final SyntaxTreeNode  getNode() { return node; }
  final boolean  isOperator() { return op != null; }
}