// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.

// last modified on Fri 16 Mar 2007 at 17:22:54 PST by lamport
package tla2sany.semantic;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import tla2sany.explorer.ExploreNode;
import tla2sany.explorer.ExplorerVisitor;
import tla2sany.parser.SyntaxTreeNode;
import tla2sany.st.Location;
import tla2sany.st.TreeNode;
import tla2sany.xml.XMLExportable;
import tlc2.value.IValue;
import tlc2.value.Values;

/**
 * SemanticNode is the (abstract) superclass of all nodes in the
 * semantic tree.
 *
 * It defines a unique identifier for each instance, which should be
 * used to check for equality.
 */
public abstract class SemanticNode
 implements ASTConstants, ExploreNode, LevelConstants, Comparable<SemanticNode>, XMLExportable /* interface for exporting into XML */ {

  private static final Object[] EmptyArr = new Object[0];

  private static final AtomicInteger uid = new AtomicInteger();  // the next unique ID for any semantic node

  protected final static ThreadLocal<Errors> errors = new ThreadLocal<>();

  public    final int      myUID;    // the unique ID of THIS semantic node
  public    TreeNode stn;      // the concrete syntax tree node associated with THIS semantic node
  private   Object[] tools;    // each tool has a location in this array where
                               //   it may store an object for its own purposes
  private   int      kind;     // indicates what kind of semantic node this is;
                               //   strongly correlated with the Java type of the node

  public SemanticNode(final int kind, final TreeNode stn) {
    myUID = uid.getAndIncrement();
    this.kind = kind;
    this.stn = stn;
    this.tools = EmptyArr;
  }

  // Use errors as a threadlocal to scope static
  public static Errors getErrors(){
      return errors.get();
  }

  public static void setErrors(final Errors errs) { errors.set(errs);}

  public static String levelToString(final int level) {
      return switch (level) {
          case ConstantLevel -> level + " (Constant)";
          case VariableLevel -> level + " (Variable)";
          case ActionLevel -> level + " (Action)";
          case TemporalLevel -> level + " (Temporal)";
          default -> level + " (Illegal)";
      };
  }

  /**
   * This returns a unique identifier for the SemanticNode object.  In
   * principle, one could simply take the object itself (i.e., a ref to
   * it) as its unique id.  However, Java makes it more convenient to
   * work with int's than with ref's.
   */
  public final int getUid() { return this.myUID; }

  /**
   * The object's tools field is a sequence of objects that can be
   * used by different tools to keep tool-specific information.  A
   * tool calls FrontEnd.getToolId() to obtain a tool number t, and it
   * calls these two methods with toolId = t to read and write this
   * information.
   */
  public final Object getToolObject(final int toolId) {
    if (this.tools.length <= toolId) return null;
    return this.tools[toolId];
  }

  /* Sets tool number toolId to obj.   */
  public final void setToolObject(final int toolId, final Object obj) {
    if (this.tools.length <= toolId) {
      final Object[] newTools = new Object[toolId+1];
      System.arraycopy(this.tools, 0, newTools, 0, this.tools.length);
      this.tools = newTools;
    }
    this.tools[toolId] = obj;
  }

  /**
   * These methods read and set the node's kind, which is a value
   * indicating what kind of node it is.  The value of the kind field
   * identifies the subclass of SemNode to which a node belongs.  All
   * the objects of a subclass have the same kind value, except for
   * OpDefOrDeclNode objects, which can have any of 6 different kinds.
   * The setKind method for each subclass checks that this is a legal
   * kind for that subclass, and raise an exception if it isn't.  See
   * the ASTConstants interface for a list of all the kinds of
   * semantic nodes.
   */
  public final int getKind() { return this.kind; }

  /* Sets the kind field of this object to k.  */
  public final void setKind(final int k) { this.kind = k; }

  /* Returns the same concrete syntax tree node. */
  public final TreeNode getTreeNode() { return this.stn; }

  /**
   * Returns the array of comments immediately preceding the first
   * token of the spec that produces this semantic node.
   */
  public String[] getPreComments() { return ((SyntaxTreeNode) this.stn).getAttachedComments() ; }

  /**
   * Returns the result of getPreComments poorly formatted for
   * printing.  To be used in the toString methods for the various
   * node types.
   */
  public String getPreCommentsAsString() { return SyntaxTreeNode.PreCommentToString(this.getPreComments()) ; }

  /**
   * This returns the context of the node in the semantic tree.  It is
   * not defined what that means.  Here's the idea behind this method.
   * Suppose a tool wants to allow the user to edit the part of a spec
   * corresponding to a SemanticNode n.  After calling parseAll to obtain
   * the new CSTNode cst, the tool will call
   *
   *    semanticAnalysis(cst, n.getKind(), n.getContext())
   *
   * to obtain the new SemanticNode newsn.  As part of "plugging" the
   * node newsn into the semantic tree, the tool can call
   * newsn.setParent(n.getParent()).  It's likely that, for an
   * arbitrary node n in the semantic tree, n.getContext() is likely
   * to be just a wrapper for a pointer to n.
   */

    /**
   *  Returns the children of this node in the semantic tree.  For a
   *  node that normally has no children, it should return null.  In
   *  general, a child of a semantic node is a SemanticNode that describes
   *  a piece of the module whose location is within the location of that
   *  node.  If it's not obvious what the children should be for some kinds
   *  of semantic node, check the method for the particular kind of node to find
   *  out what it actually returns.
   *  <p>
   *  Initially, this method is not implemented for all kinds of semantic
   *  nodes.  It will be implemented as needed for whatever we decide to
   *  use it for.  The initial implementation is for being able to
   *  walk down the semantic tree to find the definition or declaration
   *  of a symbol.
   *  <p>
   *  This should probably be optimized by adding a field to a semantic
   *  node to cache the value of getChildren() when it's computed.
   *  However, perhaps that's only necessary for a ModuleNode.
   *
   *  This default method returns null.
   */
  public SemanticNode[] getChildren() {
      return null;
  }

	/**
	 * @return Returns an empty list instead of null compared to getChildren.
	 */
	public List<SemanticNode> getListOfChildren() {
		final SemanticNode[] children = getChildren();
		if (children == null) {
			return new ArrayList<>();
		}
		// The OpDefNode#body of e.g. 'foo == INSTANCE Spec' is null, which is why
		// getChildren returns a non-zero length array containing a null value.
		//
		//  SomeExpression ==
		//    LET foo == INSTANCE Spec
		//    IN foo!bar
		//
		// We filter null values here instead of in OpDefNode#getChildren because I
		// don't know if some functionality relies on getChildren to return null values.
		return Arrays.stream(children).filter(Objects::nonNull).collect(Collectors.toList());
	}
  
	public <T> ChildrenVisitor<T> walkChildren(final ChildrenVisitor<T> visitor) {
		visitor.preVisit(this);
		for (final SemanticNode c : getListOfChildren()) {
			if (visitor.preempt(c)) {
				continue;
			}
			c.walkChildren(visitor);
		}
		return visitor.postVisit(this);
	}

	public static class ChildrenVisitor<T> {
		public void preVisit(final SemanticNode node) {
		}

		public boolean preempt(final SemanticNode node) {
			return true;
		}

		public ChildrenVisitor<T> postVisit(final SemanticNode node) {
			return this;
		}

		public T get() {
			return null;
		}
	}

	/**
	 * @return The path in the semantic tree up to the given {@link Location}
	 *         starting from this node. An empty {@link LinkedList} if no reachable
	 *         {@link SemanticNode} matches the given {@link Location}.
	 *         <p>
	 *         {@link LinkedList#getFirst()} is the {@link SemanticNode} matching
	 *         {@link Location}.
	 */
	public LinkedList<SemanticNode> pathTo(final Location location) {
		final ChildrenVisitor<LinkedList<SemanticNode>> visitor = walkChildren(
                new ChildrenVisitor<>() {
                    LinkedList<SemanticNode> pathToLoc;

                    @Override
                    public LinkedList<SemanticNode> get() {
                        return Objects.requireNonNullElseGet(pathToLoc, LinkedList::new);
                    }

                    @Override
                    public void preVisit(final SemanticNode node) {
                        if (location.equals(node.getLocation())) {
                            // node will be added to pathToLoc in postVisit!
                            pathToLoc = new LinkedList<>();
                        } else if (node instanceof final OpDefNode odn) {
                            for (final SemanticNode param : odn.getParams()) {
                                if (location.equals(param.getLocation())) {
                                    pathToLoc = new LinkedList<>();
                                    pathToLoc.add(param);
                                }
                            }
                        } else if (node instanceof final OpApplNode oan) {
                            // TODO Include oan#range aka oan#getBded... in getQuantSymbolLists?
                            for (final FormalParamNode fpn : oan.getQuantSymbolLists()) {
                                if (location.equals(fpn.getLocation())) {
                                    pathToLoc = new LinkedList<>();
                                    pathToLoc.add(fpn);
                                }
                            }
                        }
                    }

                    @Override
                    public boolean preempt(final SemanticNode node) {
                        return pathToLoc != null
                                || !node.getLocation().includes(location);
                    }

                    @Override
                    public ChildrenVisitor<LinkedList<SemanticNode>> postVisit(final SemanticNode node) {
                        if (pathToLoc != null) {
                            pathToLoc.add(node);
                        }
                        return this;
                    }
                });
		return visitor.get();
	}

  /**
   * Default implementations of walkGraph() to be inherited by subclasses
   * of SemanticNode for implementing ExploreNode interface; the purpose
   * of walkgraph is to find all reachable nodes in the semantic graph
   * and insert them in a Hashtable for use by the Explorer tool.
   */
  @Override
  public void walkGraph(final Hashtable<Integer, ExploreNode> semNodesTable, final ExplorerVisitor visitor) {
    final Integer uid = myUID;
    if (semNodesTable.get(uid) != null) return;
    semNodesTable.put(uid, this);
    visitor.preVisit(this);
    visitor.postVisit(this);
  }

  /**
   * Default implementation of toString() to be inherited by subclasses
   * of SemanticNode for implementing ExploreNode interface; the depth
   * parameter is a bound on the depth of the tree that is converted to String.
   */
  @Override
  public String toString(final int depth) {
    if (depth <= 0) return "";
    return ("  uid: " + myUID +
	    "  kind: " + (kind == -1 ? "<none>" : kinds[kind])
	    + getPreCommentsAsString());
  }
  
  public boolean isBuiltIn(Context context) {
	  return context.isBuiltIn(this);
  }

  /**
   * @see tla2sany.modanalyzer.ParseUnit#isLibraryModule()
   * @see tla2sany.semantic.StandardModules#isDefinedInStandardModule
   */
  public boolean isStandardModule() {
	  return StandardModules.isDefinedInStandardModule(this);
  }
  
  // YY's code
  public final Location getLocation() {
	  if (this.stn != null) {
		  return this.stn.getLocation();
	  }
	  return Location.nullLoc;
  }

  /**
   * This compareTo method is for use in sorting SemanticNodes in the
   * same module according to their starting location.  It returns
   * 0 (equal) iff they have the same starting location--that is,
   * if getLocation() returns locations with equal values of
   * beginLine() and  beginColumn().  Thus, compare(s1, s2) == 0
   * is NOT equivalent to equals(s1, s2).
   *
   */
  @Override
  public int compareTo(final SemanticNode s) {
       final Location loc1 = this.stn.getLocation();
       final Location loc2 = s.stn.getLocation();
       if (loc1.beginLine() < loc2.beginLine())
        {
           return -1;
       }
       if (loc1.beginLine() > loc2.beginLine()) {
           return 1;
       }
       if (loc1.beginColumn() == loc2.beginColumn()) {
           return 0;
       }
       return (loc1.beginColumn() < loc2.beginColumn())?-1:1;
  }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + kind;
		result = prime * result + myUID;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SemanticNode other = (SemanticNode) obj;
		if (kind != other.kind) {
			return false;
		}
        return myUID == other.myUID;
    }

/***************************************************************************
* XXXXX A test for getLocation() returning null should be added            *
*       to the following two toString methods.                             *
***************************************************************************/
  public final void toString(final StringBuilder sb, final String padding) {
	  final TreeNode treeNode = getTreeNode();
		if (treeNode instanceof final SyntaxTreeNode stn
				&& System.getProperty(SemanticNode.class.getName() + ".showPlainFormulae") != null) {
            sb.append(stn.getHumanReadableImage());
	  } else {
		  sb.append(this.getLocation());
	  }
  }

  @Override
  public String toString() {
	  final TreeNode treeNode = getTreeNode();
		if (treeNode instanceof final SyntaxTreeNode stn
				&& System.getProperty(SemanticNode.class.getName() + ".showPlainFormulae") != null) {
            return stn.getHumanReadableImage();
	  }
    return this.getLocation().toString();
  }

  public String getHumanReadableImage() {
	  return getLocation().toString();
  }
  
  public String toString(final IValue aValue) {
	return Values.ppr(aValue.toString());
  }

    /**
     * August 2014 - TL
     * All nodes inherit from semantic node, which just attach location to the returned node
     */

  protected Element getSemanticElement(final Document doc, final tla2sany.xml.SymbolContext context) {
      throw new UnsupportedOperationException("xml export is not yet supported for: " + getClass() + " with toString: " + toString(100));
    }

    /**
     * August 2014 - TL
     * returns location information for XML exporting as attributes to
     * the element returned by getElement
     */
    protected Element getLocationElement(final Document doc) {
      final Location loc = getLocation();
      final Element e = doc.createElement("location");
      final Element ecol = doc.createElement("column");
      final Element eline = doc.createElement("line");
      final Element fname = doc.createElement("filename");

      final Element bl = doc.createElement("begin");
      final Element el = doc.createElement("end");
      final Element bc = doc.createElement("begin");
      final Element ec = doc.createElement("end");

      bc.appendChild(doc.createTextNode(Integer.toString(loc.beginColumn())));
      ec.appendChild(doc.createTextNode(Integer.toString(loc.endColumn())));
      bl.appendChild(doc.createTextNode(Integer.toString(loc.beginLine())));
      el.appendChild(doc.createTextNode(Integer.toString(loc.endLine())));

      fname.appendChild(doc.createTextNode(stn.getFilename()));

      ecol.appendChild(bc);
      ecol.appendChild(ec);
      eline.appendChild(bl);
      eline.appendChild(el);

      e.appendChild(ecol);
      e.appendChild(eline);
      e.appendChild(fname);

      return e;
    }

    /**
     * TL - auxiliary functions
     */
    protected Element appendElement(final Document doc, final String el, final Element e2) {
      final Element e = doc.createElement(el);
      e.appendChild(e2);
      return e;
    }
    protected Element appendText(final Document doc, final String el, final String txt) {
      final Element e = doc.createElement(el);
      final Node n = doc.createTextNode(txt);
      e.appendChild(n);
      return e;
    }


    /** August 2014 - TL
     * A location element is prepannded to an implementing element
     */
  @Override
  public Element export(final Document doc, final tla2sany.xml.SymbolContext context) {
      try {
        final Element e = getSemanticElement(doc, context);
        try {
          final Element loc = getLocationElement(doc);
          e.insertBefore(loc,e.getFirstChild());
        } catch (final UnsupportedOperationException uoe) {
          uoe.printStackTrace();
          throw uoe;
        } catch (final RuntimeException ee) {
          // do nothing if no location
        }
        return e;
      } catch (final RuntimeException ee) {
        System.err.println("failed for node.toString(): " + this + "\n with error ");
        ee.printStackTrace();
        throw ee;
      }
    }
  
  	public static final SemanticNode nullSN = new NullSemanticNode();
  
	private static class NullSemanticNode extends SemanticNode {

		private NullSemanticNode() {
			super(Integer.MIN_VALUE, SyntaxTreeNode.nullSTN);
		}

		@Override
		public String levelDataToString() {
			return Integer.toString(Integer.MIN_VALUE);
		}
	}
}
