// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.parser;

import java.util.Vector;

public final class ParseErrors implements tla2sany.st.ParseErrors {
  private final Vector<ParseError> loe;

  ParseErrors() { loe = new Vector<ParseError>(); }

    final boolean empty() { return loe.isEmpty(); }

  final void push(final ParseError pe ) {
    loe.addElement( pe );
  }

  @Override
  public final tla2sany.st.ParseError[] errors() {
    final tla2sany.st.ParseError[] pes = new tla2sany.st.ParseError[ loe.size() ];
    for (int lvi = 0; lvi < pes.length; lvi++ )
      pes[ lvi ] = loe.elementAt( lvi );
    return pes;
  }
}
