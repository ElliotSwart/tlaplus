// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 15:30:14 PST by lamport
//      modified on Sun Aug  5 00:13:41 PDT 2001 by yuanyu

package tlc2.tool.liveness;

import tlc2.tool.ITool;
import tlc2.tool.TLCState;
import tlc2.util.Vect;

class LNConj extends LiveExprNode {
	private final Vect<LiveExprNode> conjs; // The conjuncts
	private int info;

	public LNConj(final int size) {
		this.conjs = new Vect<>(size);
		this.info = 0;
	}

	public LNConj(final LiveExprNode n) {
		this.conjs = new Vect<>(1);
		this.conjs.addElement(n);
		final int level = n.getLevel();
		this.info = n.containAction() ? level + 8 : level;
	}

	public LNConj(final LiveExprNode n1, final LiveExprNode n2) {
		this.conjs = new Vect<>(2);
		this.conjs.addElement(n1);
		this.conjs.addElement(n2);
		final boolean hasAct = n1.containAction() || n2.containAction();
		final int level = Math.max(n1.getLevel(), n2.getLevel());
		this.info = hasAct ? level + 8 : level;
	}

	public LNConj(final Vect<LiveExprNode> conjs) {
		this.conjs = conjs;
		boolean hasAct = false;
		int level = 0;
		for (int i = 0; i < conjs.size(); i++) {
			final LiveExprNode lexpr = conjs.elementAt(i);
			level = Math.max(level, lexpr.getLevel());
			hasAct = hasAct || lexpr.containAction();
		}
		this.info = hasAct ? level + 8 : level;
	}

	public final int getCount() {
		return this.conjs.size();
	}

	public final LiveExprNode getBody(final int i) {
		return this.conjs.elementAt(i);
	}

	public final void addConj(final LiveExprNode elem) {
		if (elem instanceof LNConj) {
			final LNConj elem1 = (LNConj) elem;
			for (int i = 0; i < elem1.getCount(); i++) {
				this.addConj(elem1.getBody(i));
			}
		} else {
			this.conjs.addElement(elem);
		}
		final int level = Math.max(this.getLevel(), elem.getLevel());
		final boolean hasAct = this.containAction() || elem.containAction();
		this.info = hasAct ? level + 8 : level;
	}

	public final int getLevel() {
		return (this.info & 7);
	}

	public final boolean containAction() {
		return (this.info & 8) > 0;
	}
	
	@Override
	public final boolean isPositiveForm() {
		for (int i = 0; i < conjs.size(); i++) {
			final LiveExprNode lexpr = conjs.elementAt(i);
			if (!lexpr.isPositiveForm()) {
				return false;
			}
		}
		return true;
	}

	public final boolean eval(final ITool tool, final TLCState s1, final TLCState s2) {
		final int sz = this.conjs.size();
		for (int i = 0; i < sz; i++) {
			final LiveExprNode item = this.conjs.elementAt(i);
			if (!item.eval(tool, s1, s2)) {
				return false;
			}
		}
		return true;
	}

	public final void toString(final StringBuffer sb, final String padding) {
		final int len = this.getCount();
		final String padding1 = padding + "    ";
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				sb.append(padding);
			}
			sb.append("/\\ (");
			this.getBody(i).toString(sb, padding1);
			sb.append(")");
			if (i != len - 1) {
				sb.append("\n");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see tlc2.tool.liveness.LiveExprNode#toDotViz()
	 */
	public String toDotViz() {
		final int len = this.getCount();
		final StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			sb.append("/\\ (");
			sb.append(this.getBody(i).toDotViz());
			sb.append(")");
			if (i != len - 1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public void extractPromises(final TBPar promises) {
		getBody(0).extractPromises(promises);
		getBody(1).extractPromises(promises);
	}

	public int tagExpr(int tag) {
		for (int i = 0; i < getCount(); i++) {
			tag = getBody(i).tagExpr(tag);
		}
		return tag;
	}

	public final LiveExprNode makeBinary() {
		if (getCount() == 1) {
			return getBody(0).makeBinary();
		}
		final int mid = getCount() / 2;
		final LNConj left = new LNConj(0);
		final LNConj right = new LNConj(0);
		for (int i = 0; i < getCount(); i++) {
			if (i < mid) {
				left.addConj(getBody(i));
			} else {
				right.addConj(getBody(i));
			}
		}
		return new LNConj(left.makeBinary(), right.makeBinary());
	}

	public LiveExprNode flattenSingleJunctions() {
		if (getCount() == 1) {
			return getBody(0).flattenSingleJunctions();
		}
		final LNConj lnc2 = new LNConj(getCount());
		for (int i = 0; i < getCount(); i++) {
			lnc2.addConj(getBody(i).flattenSingleJunctions());
		}
		return lnc2;
	}

	/**
	 * The method toDNF turns a LiveExprNode into disjunctive normal form.
	 */
	public final LiveExprNode toDNF() {
		final int count = getCount();
		final LiveExprNode[] temp = new LiveExprNode[count];
		for (int i = 0; i < count; i++) {
			temp[i] = getBody(i).toDNF();
		}

		// We now construct the cross product:
		final Vect<LiveExprNode> nes = new Vect<>(count);
		int total = 1;
		for (int i = 0; i < count; i++) {
			final LiveExprNode elem = temp[i];
			if (elem instanceof LNDisj) {
				nes.addElement(elem);
				total *= ((LNDisj) elem).getCount();
			} else if (elem instanceof LNConj) {
				// Flatten when elem is also a LNConj:
				final LNConj elem1 = (LNConj) elem;
				final int count1 = elem1.getCount();
				for (int j = 0; j < count1; j++) {
					nes.addElement(elem1.getBody(j));
				}
			} else {
				nes.addElement(elem);
			}
		}

		if (total == 1) {
			return new LNConj(nes);
		}
		final int nesSize = nes.size();
		final Vect<LiveExprNode> res = new Vect<>(total);
		for (int i = 0; i < total; i++) {
			res.addElement(new LNConj(nesSize));
		}
		int num = 1;
		int rCount = total;
		for (int i = 0; i < nesSize; i++) {
			final LiveExprNode ln = nes.elementAt(i);
			if (ln instanceof LNDisj) {
				final LNDisj disj = (LNDisj) ln;
				rCount = rCount / disj.getCount();
				int idx = 0;
				for (int j = 0; j < num; j++) {
					for (int k = 0; k < disj.getCount(); k++) {
						final LiveExprNode elem = disj.getBody(k);
						for (int l = 0; l < rCount; l++) {
							((LNConj) res.elementAt(idx++)).addConj(elem);
						}
					}
				}
				num = num * disj.getCount();
			} else {
				for (int j = 0; j < total; j++) {
					((LNConj) res.elementAt(j)).addConj(ln);
				}
			}
		}
		return new LNDisj(res);
	}

	public LiveExprNode simplify() {
		final LNConj lnc1 = new LNConj(getCount());
		for (int i = 0; i < getCount(); i++) {
			final LiveExprNode elem = getBody(i).simplify();
			if (elem instanceof LNBool) {
				if (!((LNBool) elem).b) {
					return LNBool.FALSE;
				}
			} else {
				lnc1.addConj(elem);
			}
		}
		if (lnc1.getCount() == 0) {
			return LNBool.TRUE;
		}
		if (lnc1.getCount() == 1) {
			return lnc1.getBody(0);
		}
		return lnc1;
	}

	public boolean isGeneralTF() {
		for (int i = 0; i < getCount(); i++) {
			if (!getBody(i).isGeneralTF()) {
				return false;
			}
		}
		return super.isGeneralTF();
	}

	public LiveExprNode pushNeg() {
		final LNDisj lnd = new LNDisj(getCount());
		for (int i = 0; i < getCount(); i++) {
			lnd.addDisj(getBody(i).pushNeg());
		}
		return lnd;
	}

	public LiveExprNode pushNeg(final boolean hasNeg) {
		if (hasNeg) {
			final LNDisj lnd = new LNDisj(getCount());
			for (int i = 0; i < getCount(); i++) {
				lnd.addDisj(getBody(i).pushNeg(true));
			}
			return lnd;
		} else {
			final LNConj lnc1 = new LNConj(getCount());
			for (int i = 0; i < getCount(); i++) {
				lnc1.addConj(getBody(i).pushNeg(false));
			}
			return lnc1;
		}
	}

	public boolean equals(final LiveExprNode exp) {
		if (exp instanceof LNConj) {
			final LNConj exp2 = (LNConj) exp;
			if (getCount() != exp2.getCount()) {
				return false;
			}
			for (int i = 0; i < getCount(); i++) {
				if (!getBody(i).equals(exp2.getBody(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
