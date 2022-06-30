// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tla2sany.modanalyzer;

import tla2sany.utilities.Vector;

/**
 * This class contains fields representing the names of modules that are all related by EXTENDing or INSTANCEing
 * or inner definition to a ParseUnit (i.e. top level module in its own file)
 */
public class ModuleRelatives {

  final ParseUnit     parseUnit;                              // The ParseUnit that THIS ModuleRelatives object is associated with

  ModulePointer currentModule                   = null; // The TreeNode where the name of the current module appears in
                                                        //   in the image field; using the tree node rather than the String
                                                        //   name of the module 

  ModulePointer outerModule                     = null; // TreeNode of the immediate outer (parent) module; 
                                                        //   null currentModule is the outermost in parseUnit

  final Vector<ModulePointer>        directInnerModules              = new Vector<>();
                                                        // Vector of ModulePointers for immediate inner modules 

  final Vector<String>        directlyExtendedModuleNames     = new Vector<>();
                                                        // Vector of String names for modules mentioned in EXTENDS decls by 
                                                        //   currentModule, whether or not they are resolved within the 
                                                        //   current ParseUnit

  final Vector<String>        directlyInstantiatedModuleNames = new Vector<>();
                                                        // Vector of String names for modules directly instantiated 
                                                        //   by currentModule, whether or not they are resolved within the
                                                        //   current ParseUnit

  final ModuleContext context = new ModuleContext();          // The context that maps module String names known in this module
                                                        //   (whether or not they are referenced in it) to ModulePointers


  // constructor
  public ModuleRelatives(final ParseUnit pu, final ModulePointer node) {
    parseUnit     = pu;
    currentModule = node;
  }

  public String toString() {
    StringBuilder ret = new StringBuilder("currentModuleName: " + currentModule.getName());

    ret.append("\nouterModule: ").append(outerModule == null
            ? "<null>"
            : outerModule.getName());

    ret.append("\ndirectInnerModules: ");
    for (int i = 0; i < directInnerModules.size(); i++) {
      ret.append((directInnerModules.elementAt(i)).getName()).append(" ");
    }

    ret.append("\ndirectlyExtendedModuleNames: ");
    for (int i = 0; i < directlyExtendedModuleNames.size(); i++) {
      ret.append(directlyExtendedModuleNames.elementAt(i)).append(" ");
    }

    ret.append("\ndirectlyInstantiatedModuleNames: ");
    for (int i = 0; i < directlyInstantiatedModuleNames.size(); i++) {
      ret.append(directlyInstantiatedModuleNames.elementAt(i)).append(" ");
    }

    ret.append("\n").append(context);
    return ret.toString();
  }

	public void addExtendee(final String module) {
		directlyExtendedModuleNames.addElement(module);
	}
} // end class
