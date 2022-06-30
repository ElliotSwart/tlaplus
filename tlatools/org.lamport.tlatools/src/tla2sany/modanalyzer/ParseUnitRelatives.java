// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tla2sany.modanalyzer;

import tla2sany.utilities.Vector;

class ParseUnitRelatives {

  final Vector<ParseUnit> extendees  = new Vector<>();  // vector of ParseUnit objects

  final Vector<ParseUnit> extendedBy = new Vector<>();  // vector of ParseUnit objects

  final Vector<ParseUnit> instancees = new Vector<>();  // vector of ParseUnit objects

  final Vector<ParseUnit> instancedBy = new Vector<>();  // vector of ParseUnit objects

  public final String toString() {
    return "[ extendees = "   + extendees.toString() +
           ", extendedBy = "  + extendedBy.toString() +
           ", instancees = "  + instancees.toString() +
           ", instancedBy = " + instancedBy.toString() +
           " ]";
  }

}
