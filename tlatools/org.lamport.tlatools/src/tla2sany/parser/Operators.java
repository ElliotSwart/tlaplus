// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Last modified onFri  2 Mar 2007 at 15:40:00 PST by lamport
/***************************************************************************
* 2 Mar 2007: enum <- Enum                                                 *
***************************************************************************/

package tla2sany.parser;

import java.util.Enumeration;
import java.util.Hashtable;

import util.UniqueString;

public class Operators {
  static public final int assocNone = 0;
  static public final int assocLeft = 1;
  static public final int assocRight = 2;

  /*************************************************************************
  * The following appear to be classes of operators.                       *
  *************************************************************************/
    /***********************************************************************
    * What is a nofix operator?????????                                    *
    ***********************************************************************/
  static public final int prefix = 1;
  static public final int postfix = 2;
  static public final int infix = 3;
  static public final int nfix = 4;// \X
    /***********************************************************************
    * The only operator of class nfix seems to be \X (aka \times).         *
    ***********************************************************************/
  static final Hashtable<UniqueString, Operator> DefinitionTable = new Hashtable<>();
    /***********************************************************************
    * Contains the Operator objects for all operators.  It is constructed  *
    * from the data in ConfigConstants.defaultConfig.                      *
    ***********************************************************************/
  static final Hashtable<UniqueString, UniqueString> BuiltinTable = new Hashtable<>();
    /***********************************************************************
    * It appears that this is not used.                                    *
    ***********************************************************************/
    
  static public void addOperator(final UniqueString name, final Operator op ) {
    DefinitionTable.put(name, op);
  }

  static public Operator getOperator(final UniqueString name ) {
    return DefinitionTable.get( name );
  }

  static public Operator getMixfix(final Operator op ) {
     if (op.isPrefix()) return op;
     else {
       final UniqueString id = UniqueString.uniqueStringOf( op.getIdentifier().toString() + ".");
       return DefinitionTable.get( id );
     }
  }
  
  

  static public void addSynonym(final UniqueString template, final UniqueString match ) {
    /*
       do make sure that the operator already exists.
       We make the new definition point to the other one.
    */
    final Operator n = DefinitionTable.get( match );
    if (n != null) {
      DefinitionTable.put(template, n);
    } /* else {
       error
    } */
  }
  
  /*************************************************************************
  * resolveSynonym has the property that                                   *
  *                                                                        *
  *    resolveSynonym(a) = resolveSynonym(b)                               *
  *                                                                        *
  * iff either a = b or a and b are synonyms (like (+) and \oplus).  If a  *
  * has no synonmys, then resolveSynonym(a) = a.                           *
  *************************************************************************/
  static public UniqueString resolveSynonym(final UniqueString name ) {
    final Operator n = DefinitionTable.get( name );
    if ( n == null ) return name;
    else return n.getIdentifier();
  }

  

  /*************************************************************************
  * It appears that the following method is not used.                      *
  *************************************************************************/
  static public UniqueString getBuiltinAssoc(final UniqueString symbol ) {
    /* first, resolve synonyms */
    final Operator n = DefinitionTable.get(symbol);
    if (n != null) {
      final UniqueString name = n.getIdentifier(); /* can't be null */
      /* then lookup solution */
      return (BuiltinTable.get(name));
    } else
      return null;
  }

/* debugging help */
  static public void printTable() {
    System.out.println("printing Operators table");
    final Enumeration<UniqueString> Enum = DefinitionTable.keys();
    while( Enum.hasMoreElements() ) { System.out.println("-> " + Enum.nextElement().toString() ); }
  }

// shouldn't be necessary

}
