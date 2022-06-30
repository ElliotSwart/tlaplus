// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tla2sany.modanalyzer;

import tla2sany.parser.SyntaxTreeNode;
import tla2sany.st.Location;
import tla2sany.st.ParseTree;
import tla2sany.st.TreeNode;

public class SyntaxTreePrinter {

  /** 
   *  This method is used in only one place--in the ParseUnit class--to write a copy
   *  of the syntax tree to a file in case that option is invoked.
   */
  static public final void print(final ParseTree pt, final java.io.PrintWriter output ) {
    output.println("%% Output of parse tree for module " + pt.moduleName() );
    final String[] dependencies = pt.dependencies();
    if (dependencies.length == 0) {
      output.println("%% no dependencies");
    } else {
      output.print("%% dependends on:");
      for (int lvi = 0; lvi < dependencies.length; lvi++) {
        output.print(" " + dependencies[lvi]);
      }
      output.println(".");
    }
    printSubTree(output, "", pt.rootNode() );
  }

  /*
  static public final void print( TreeNode tn, java.io.PrintWriter output ) {
    output.println("%% Output of syntax tree" ); 
    printSubTree(output, "", tn);
  }
  */
   
  private static void printSubTree(final java.io.PrintWriter o, final String offset, final TreeNode node ) {
    final StringBuffer outS = new StringBuffer( offset );
    final Location l = node.getLocation();
    final String image = node.getImage();
    if (image != null ) {
      outS.append(image);
    } else {
      outS.append("-- no name --");
    }
    outS.append(" [" ).append(l.beginLine()).append(" ").append(l.beginColumn()).append("] ");  
    final TreeNode[] h = node.heirs();
// ADDED BY LL
// if (h == null) {
  outS.append(" (kind: ").append(node.getKind()).append(") ");
// } ;
// END ADDED BY LL
    if (h != null) {
      if (h.length == 0 ) {
        final int length = node.getPreComments().length;
        outS.append(length); outS.append(" pre-comments ");
// Commented out on 21 Aug 2007 by LL
//        length = node.getPostComments().length;
//        outS.append(length); outS.append(" post-comments ");
      }
      outS.append(" {");
      o.println(outS);
      for (int i=0; i<h.length; i++) {
        printSubTree(o, offset + ".", h[i] );
      }
      o.print(offset); o.println( "}");
    } else {
      outS.append(" ***WARNING***  null array reference ");
      o.println(outS);
    }
  }

}

