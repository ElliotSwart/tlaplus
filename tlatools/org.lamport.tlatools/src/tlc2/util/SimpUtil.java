// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:39 PST by lamport
//      modified on Mon Jun 28 14:52:12 PDT 1999 by yuanyu

package tlc2.util;

import tlc2.tool.ToolGlobals;

public class SimpUtil implements ToolGlobals {

  public static Sx store = Sx.Atom("store");
  public static Sx select = Sx.Atom("select");
  public static Sx t = Sx.Atom("|@true|");
  public static Sx f = Sx.Atom("|@false|");
  public static Sx forall = Sx.Atom("FORALL");
  public static Sx exists = Sx.Atom("EXISTS");
  public static Sx f_eq = Sx.Atom("F_EQ");
  public static Sx implies = Sx.Atom("IMPLIES");
  public static Sx f_implies = Sx.Atom("F_IMPLIES");  
  public static Sx and = Sx.Atom("AND");
  public static Sx f_and = Sx.Atom("F_AND");  
  public static Sx or = Sx.Atom("OR");
  public static Sx f_or = Sx.Atom("F_OR");
  public static Sx iff = Sx.Atom("IFF");  
  public static Sx eq = Sx.Atom("EQ");
  public static Sx in = Sx.Atom("in");
  public static Sx empty = Sx.Atom("EMPTY");
  public static Sx mkdom = Sx.Atom("mkdom");
  public static Sx intv = Sx.Atom("intv");
  public static Sx cond = Sx.Atom("cond");
  public static Sx cross2 = Sx.Atom("cross2");
  public static Sx bgPush = Sx.Atom("BG_PUSH");
  public static Sx pats = Sx.Atom("PATS");  

  // The following list contains the Sx translations of the TLA expressions in
  // the input module.
  public static Sx opDefns = Sx.nil;
  public static Sx fcnDefns = Sx.nil;
  public static Sx thms = Sx.nil;

  public static Sx mkForall(Sx vars, Sx body) {
    return Sx.List(forall, vars, body);
  }

  public static Sx mkForall(Sx vars, Sx pats, Sx body) {
    return Sx.List(forall, vars, pats, body);
  }

  public static Sx mkImplies(Sx p, Sx q) {
    return Sx.List(f_implies, p, q);
  }

  public static Sx mkAnd(Sx p, Sx q) {
    return Sx.List(f_and, p, q);
  }

  public static Sx mkEquality(Sx p, Sx q) {
    return Sx.List(eq, p, q);
  }

  public static Sx mkInterval(Sx i, Sx j) {
    return Sx.List(intv, i, j);
  }

  public static Sx mkIn(Sx x, Sx s) {
    return Sx.List(in, x, s);
  }

  public static Sx mkIff(Sx x, Sx s) {
    return Sx.List(iff, x, s);
  }
  
  public static Sx mkBgPush(Sx x) {
    return Sx.List(bgPush, x);
  }  
}
