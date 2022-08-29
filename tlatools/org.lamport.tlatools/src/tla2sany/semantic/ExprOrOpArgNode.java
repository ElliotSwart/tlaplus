// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tla2sany.semantic;

import tla2sany.st.TreeNode;

/**
 * This class represents a semantic node that is either an OpArgNode
 * or is the root node of an expression (ExprNode).
 * <p>
 * The reason both kinds of nodes are classified together in this
 * abstract class is that they can both be used as arguments to
 * operators and in substitutions.
 * <p>
 * Extended by ExprNode and OpArgNode
 * <p>
 * Further extended by AtNode, DecimalNode, LetInNode, NumeralNode,
 * OpApplNode, StringNode, SubstInNode
 */
public abstract class ExprOrOpArgNode extends LevelNode {

    ExprOrOpArgNode(final int kind, final TreeNode stn) {
        super(kind, stn);
    }

}
