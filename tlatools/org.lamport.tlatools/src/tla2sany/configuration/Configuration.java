// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
/* Generated By:JavaCC: Do not edit this line. Configuration.java */

/***************************************************************************
* This file was originally created by running javacc on the grammar file   *
* config.jj that specifies the parsing of the string                       *
* ConfigConstants.defaultConfig.  The current file has since been          *
* modified by DRJ (David Jefferson).                                       *
*                                                                          *
* This file was modified by LL on 13 May 2008 to replace calls to          *
* System.out and System.err by calls to ToolIO.out and ToolIO.err.         *
***************************************************************************/
// Last modified on Tue 13 May 2008 at  1:08:30 PST by lamport


package tla2sany.configuration;

import java.io.File;

import tla2sany.parser.Operator;
import tla2sany.parser.Operators;
import tla2sany.parser.SyntaxTreeNode;
import tla2sany.semantic.AbortException;
import tla2sany.semantic.Context;
import tla2sany.semantic.Errors;
import tla2sany.semantic.FormalParamNode;
import tla2sany.semantic.OpDefNode;
import tla2sany.st.Location;
import util.ToolIO;
import util.UniqueString;

public final class Configuration implements ConfigConstants {

  private static Errors         errors;
  private static java.io.Reader input;

  public static void displayDefinitions() {
    ToolIO.out.println( defaultConfig );
  }

  @SuppressWarnings("unused")
public static void load (final Errors errs ) throws AbortException {
    /***********************************************************************
    * Called from drivers/SANY.java                                        *
    ***********************************************************************/
    final Configuration Parser;
    try {
      errors = errs;
      final File source = new File( "config.src" );
      final String origin;

      if ( source.exists() ) {
//      java.io.OutputStream output;
        input = new java.io.FileReader( source );
        origin = " from local config.src file.";
      } else {
        input = new java.io.StringReader( defaultConfig );
        origin = " from defaults.";
      }
      Parser = new Configuration( input );

      try {
        Configuration.ConfigurationUnit();
//      Operators.printTable();
      } catch (final ParseException e) {
        errors.addAbort(Location.nullLoc,"\nConfiguration Parser:  Encountered errors during parse.  " 
                        + e.getMessage(),true );
      }

    } catch (final java.io.FileNotFoundException e) {
      errors.addAbort(Location.nullLoc,"File not found.\n" + e,true);
    }
  } // end load()



  static final public void ConfigurationUnit() throws ParseException, AbortException {
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OPERATOR:
      case SYNONYM:
      case BUILTIN:
          break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      OpDefinition();
    }
  }


  static final public void OpDefinition() throws ParseException, AbortException {
  final Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OPERATOR:
      jj_consume_token(OPERATOR);
      t = jj_consume_token(OPID);

      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NUMBER:
        OpBody(t.image);
        break;
      case NOTOP:
        OpNull(t.image);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
    case SYNONYM:
      OpSynonym();
      break;
    case BUILTIN:
      OpBuiltin();
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

 static final public void OpBody(final String s) throws ParseException {
 Token t;
 final int kind;
     final int assoc;
     final int low;
     final int high;
     t = jj_consume_token(NUMBER);
                low = Integer.parseInt( t.image );
    t = jj_consume_token(NUMBER);
                high = Integer.parseInt( t.image );
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LEFTASSOC:
      jj_consume_token(LEFTASSOC);
                   assoc = Operators.assocLeft;
      break;
    case RIGHTASSOC:
      jj_consume_token(RIGHTASSOC);
                    assoc = Operators.assocRight;
      break;
    case NOASSOC:
      jj_consume_token(NOASSOC);
                 assoc = Operators.assocNone;
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INFIX:
      jj_consume_token(INFIX);
               kind = Operators.infix;
      break;
    case PREFIX:
      jj_consume_token(PREFIX);
                kind = Operators.prefix;
      break;
    case POSTFIX:
      jj_consume_token(POSTFIX);
                 kind = Operators.postfix;
      break;
    case NFIX:
      jj_consume_token(NFIX);
              kind = Operators.nfix;
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
     t = null;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OPID:
      t = jj_consume_token(OPID);
      break;
    default:
      jj_la1[5] = jj_gen;
    }
   final Operator op;
   if ( t == null ) {
     op = new Operator( UniqueString.uniqueStringOf(s), low, high, assoc, kind );
   } else {
     op = new Operator( UniqueString.uniqueStringOf(t.image), low, high, assoc, kind );
   }
   Operators.addOperator( UniqueString.uniqueStringOf(s), op );
  }

  static final public void OpSynonym() throws ParseException {
  final Token t1;
      final Token t2;
      jj_consume_token(SYNONYM);
    t1 = jj_consume_token(OPID);
    t2 = jj_consume_token(OPID);
    Operators.addSynonym( UniqueString.uniqueStringOf(t1.image), 
                          UniqueString.uniqueStringOf(t2.image) );
  }

  static final public void OpNull(final String s) throws ParseException {
    jj_consume_token(NOTOP);
  }

  static final public void OpBuiltin() throws ParseException, AbortException {
  Token t;
  final String external;
  final UniqueString us;
    jj_consume_token(BUILTIN);
    t = jj_consume_token(OPID);
    external = t.image; us = UniqueString.uniqueStringOf( external );
    t = jj_consume_token(RESTRICTED);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INFIX:
      jj_consume_token(INFIX);
      Context.addGlobalSymbol( us, new OpDefNode( us, tla2sany.semantic.ASTConstants.BuiltInKind, 2,
                        new FormalParamNode[2], false, null, null, null, new SyntaxTreeNode( us ) ),
                        errors);
      break;
    case PREFIX:
      jj_consume_token(PREFIX);
      Context.addGlobalSymbol( us, new OpDefNode( us, tla2sany.semantic.ASTConstants.BuiltInKind, 1,
                        new FormalParamNode[1], false, null, null, null, new SyntaxTreeNode( us ) ),
                        errors);
      break;
    case POSTFIX:
      jj_consume_token(POSTFIX);
      Context.addGlobalSymbol( us, new OpDefNode( us, tla2sany.semantic.ASTConstants.BuiltInKind, 1,
                        new FormalParamNode[1], false, null, null, null, new SyntaxTreeNode( us ) ),
                        errors);
      break;
    case CONSTANT:
      jj_consume_token(CONSTANT);
      Context.addGlobalSymbol( us, new OpDefNode( us, tla2sany.semantic.ASTConstants.BuiltInKind, 0,
                        new FormalParamNode[0], false, null, null, null, new SyntaxTreeNode( us ) ),
                        errors);
      break;
    case NUMBER:
      t = jj_consume_token(NUMBER);
      final int n = Integer.parseInt( t.image );
      FormalParamNode fpn[] = null;
      if ( n != -1 ) fpn = new FormalParamNode[ n ];
      Context.addGlobalSymbol( us, 
                        new OpDefNode( us, tla2sany.semantic.ASTConstants.BuiltInKind, n,
                                       fpn, false, null, null, null, new SyntaxTreeNode( us ) ),
                                       errors);
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }

  }

  static private boolean                  jj_initialized_once = false;
  static public ConfigurationTokenManager token_source;
  static ASCII_CharStream                 jj_input_stream;
  static public Token                     token, jj_nt;
  static private int                      jj_ntk;
  static private int                      jj_gen;
  static final private int[]              jj_la1 = new int[7];
  static final private int[]              jj_la1_0 = 
                                            {0x44100,0x402000,0x44100,0x38000,
                                             0x1e00,0x200000,0x400e80,};

  public Configuration(final java.io.InputStream stream) {
    if (jj_initialized_once) {
      ToolIO.out.println("ERROR: Second call to constructor of static parser.  You must");
      ToolIO.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      ToolIO.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new ConfigurationTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static public void ReInit(final java.io.InputStream stream) {
    jj_initialized_once = false;
    ASCII_CharStream.ReInit(stream, 1, 1);
    ConfigurationTokenManager.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  public Configuration(final java.io.Reader stream) {
    if (jj_initialized_once) {
      ToolIO.out.println("ERROR: Second call to constructor of static parser.  You must");
      ToolIO.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      ToolIO.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new ASCII_CharStream(stream, 1, 1);
    token_source = new ConfigurationTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static public void ReInit(final java.io.Reader stream) {
    jj_initialized_once = false;
    ASCII_CharStream.ReInit(stream, 1, 1);
    ConfigurationTokenManager.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  // The following method added by DRJ.  It should be in the .jcc version of this file
  static public void ReInit() {
    jj_initialized_once = false;
    ASCII_CharStream.ReInit(input, 1, 1);
    ConfigurationTokenManager.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }


  public Configuration(final ConfigurationTokenManager tm) {
    if (jj_initialized_once) {
      ToolIO.out.println("ERROR: Second call to constructor of static parser.  You must");
      ToolIO.out.println("       either use ReInit() or set the JavaCC option STATIC to false");
      ToolIO.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  public void ReInit(final ConfigurationTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  static final private Token jj_consume_token(final int kind) throws ParseException {
    final Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = ConfigurationTokenManager.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = ConfigurationTokenManager.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  static final public Token getToken(final int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = ConfigurationTokenManager.getNextToken();
    }
    return t;
  }

  static final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=ConfigurationTokenManager.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  static final public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    final boolean[] la1tokens = new boolean[24];
    for (int i = 0; i < 24; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 7; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    final int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  static final public void enable_tracing() {
  }

  static final public void disable_tracing() {
  }

}
