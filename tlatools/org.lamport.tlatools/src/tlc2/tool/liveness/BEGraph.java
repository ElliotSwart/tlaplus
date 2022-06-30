// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:33:31 PST by lamport
//      modified on Wed Dec  5 22:41:28 PST 2001 by yuanyu

package tlc2.tool.liveness;

import tlc2.output.EC;
import tlc2.tool.EvalException;
import tlc2.util.MemObjectQueue;
import tlc2.util.MemObjectStack;
import tlc2.util.Vect;

public class BEGraph {
	/**
	 * BEGraph represents the behaviour graph.
	 */
	public Vect<BEGraphNode> initNodes;
	public String metadir;
	public NodeTable allNodes;

	public BEGraph(final String metadir, final boolean isBT) {
		this.initNodes = new Vect<>();
		this.metadir = metadir;
		this.allNodes = new NodeTable(127, isBT);
	}

	/**
	 * This method resets the number field of all nodes in this behavior graph
	 * to 0. Assume that all the nodes have non-zero number fields.
	 */
	public final void resetNumberField() {
		final MemObjectStack stack = new MemObjectStack(this.metadir, "resetstack");
		for (int i = 0; i < this.initNodes.size(); i++) {
			final BEGraphNode node = this.initNodes.elementAt(i);
			if (node.resetNumberField() != 0) {
				stack.push(this.initNodes.elementAt(i));
			}
		}
		while (stack.size() != 0) {
			final BEGraphNode node = (BEGraphNode) stack.pop();
			for (int i = 0; i < node.nextSize(); i++) {
				final BEGraphNode node1 = node.nextAt(i);
				if (node1.resetNumberField() != 0) {
					stack.push(node1);
				}
			}
		}
	}

	/* Returns the ith initial node. */
	public final BEGraphNode getInitNode(final int i) {
		return this.initNodes.elementAt(i);
	}

	public final void addInitNode(final BEGraphNode node) {
		this.initNodes.addElement(node);
	}

	/* Returns the number of initial nodes. */
	public final int initSize() {
		return this.initNodes.size();
	}

	/* Returns the shortest path from start to end (inclusive). */
	public static BEGraphNode[] getPath(final BEGraphNode start, final BEGraphNode end) {
		if (start.equals(end)) {
			start.setParent(null);
		} else {
			final boolean unseen = start.getVisited();
			final MemObjectQueue queue = new MemObjectQueue(null); // bomb if
			// checkpoint
			start.flipVisited();
			queue.enqueue(new NodeAndParent(start, null));
			boolean found = false;
			while (!found) {
				final NodeAndParent np = (NodeAndParent) queue.dequeue();
				if (np == null) {
					throw new EvalException(EC.TLC_LIVE_BEGRAPH_FAILED_TO_CONSTRUCT);
				}
				final BEGraphNode curNode = np.node;
				for (int i = 0; i < curNode.nextSize(); i++) {
					final BEGraphNode nextNode = curNode.nextAt(i);
					if (nextNode.getVisited() == unseen) {
						if (nextNode.equals(end)) {
							end.setParent(curNode);
							found = true;
							break;
						}
						nextNode.flipVisited();
						queue.enqueue(new NodeAndParent(nextNode, curNode));
					}
				}
				curNode.setParent(np.parent);
			}
		}
		// Get the path following parent pointers:
		final Vect<BEGraphNode> path = new Vect<>();
		BEGraphNode curNode = end;
		while (curNode != null) {
			path.addElement(curNode);
			curNode = curNode.getParent();
		}
		final int sz = path.size();
		final BEGraphNode[] bpath = new BEGraphNode[sz];
		for (int i = 0; i < sz; i++) {
			bpath[i] = path.elementAt(sz - i - 1);
		}
		return bpath;
	}

	/**
	 * This method assumes that the visited field of all the nodes is set to the
	 * same value.
	 */
	public final String toString() {
		final StringBuffer buf = new StringBuffer();
		final int sz = this.initNodes.size();
		if (sz != 0) {
			final boolean seen = this.getInitNode(0).getVisited();
			for (int i = 0; i < this.initNodes.size(); i++) {
				final BEGraphNode node = this.getInitNode(i);
				node.toString(buf, seen);
			}
		}
		return buf.toString();
	}

	private static class NodeAndParent {
		BEGraphNode node;
		BEGraphNode parent;

		NodeAndParent(final BEGraphNode node, final BEGraphNode parent) {
			this.node = node;
			this.parent = parent;
		}
	}
}
