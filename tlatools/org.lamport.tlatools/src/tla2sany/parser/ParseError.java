// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.parser;


public class ParseError implements tla2sany.st.ParseError {
  private String custom;
  private String backup;
  ParseError(final String a, final String b) {
    custom = a;
    backup = b;
  }

  ParseError(final String a) {
    custom = a;
    backup = "";
  }

  public final String reportedError() { return custom; };
  public final String defaultError() { return backup; };
}

