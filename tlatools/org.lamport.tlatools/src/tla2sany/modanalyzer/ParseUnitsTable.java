// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tla2sany.modanalyzer;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Each instance of this class basically is a map from ParseUnit objects to
 * ParseUnitRelatives objects.
 *
 * It is just a wrapper for a Hashtable, with the added benefit of type checking
 */

class ParseUnitsTable {

  // Maps ParseUnit string names to their respective ParseUnit objects
  final Hashtable<ParseUnit, ParseUnit> parseUnitTable = new Hashtable<>();

  ParseUnit get(final ParseUnit parseUnitName) {
    return parseUnitTable.get(parseUnitName); 
  }

  void put (final ParseUnit parseUnitName, final ParseUnit parseUnit) {
    parseUnitTable.put(parseUnitName, parseUnit);
  }

  Enumeration<ParseUnit> getKeys() { return parseUnitTable.keys(); }  

  public String toString() {
    final StringBuilder ret = new StringBuilder();

    final Enumeration<ParseUnit> e = parseUnitTable.keys();
    while ( e.hasMoreElements()) {
      ret.append("[ ParseUnit: ").append(e.nextElement().getName()).append(" ] ");
    }
    return ret.toString();
  }

}
