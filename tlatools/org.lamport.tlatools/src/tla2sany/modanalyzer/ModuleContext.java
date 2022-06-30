// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.

// Last modified onFri  2 Mar 2007 at 15:40:00 PST by lamport
/***************************************************************************
* 2 Mar 2007: enum <- Enum                                                 *
***************************************************************************/

package tla2sany.modanalyzer;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An instance of this class is the ModuleContext for a module, i.e. the mapping between
 * module String names and other ModulePointers elsewhere in the specification that they 
 * are bound to.
 * 
 * There is no distinction made between a String that is not the name of a module known
 * in this context, and an as-yet unbound name that is known.  In both cases resolve()
 * returns null.
 */
public class ModuleContext {

  Hashtable<String, ModulePointer> context = new Hashtable<String, ModulePointer>();

  /** 
   * Find the ModulePointer that the String modName resolves to;
   * Return null if either modName is not found in the context or
   * if it is found and resolves to null, i.e. is not yet resolved.
   */

  ModulePointer resolve(final String modName ) {
    return context.get(modName);
  }

  

 /**
   * Bind a module name to a particular ModulePointer iff that name is not
   * already bound; otherwise no-op.
   */
  void bindIfNotBound(final String modName, final ModulePointer modPointer) {
    if (context.get(modName) == null) context.put(modName,modPointer);
  }

  /**
   * Add elements of unionee ModuleContext to THIS ModuleContext,
   * without overwriting in cases where keys overlap between THIS and unionee
   */
  void union (final ModuleContext unionee) {

    final Enumeration<String> Enum = unionee.context.keys();
    while ( Enum.hasMoreElements() ) {
      final String key = Enum.nextElement();
      this.bindIfNotBound(key,unionee.resolve(key));
    }

  }

  public String toString() {
    String ret = "Context:\n";
    final Enumeration<String> Enum = context.keys();

    while (Enum.hasMoreElements()) {
      final String key = Enum.nextElement();
      final ModulePointer modPointer = context.get(key);

      ret = ret + "  " + key + "-->" + (modPointer != null ? modPointer.toStringAbbrev() : "null");
    }

    return ret;

  } // end toString()
    

} // end class
