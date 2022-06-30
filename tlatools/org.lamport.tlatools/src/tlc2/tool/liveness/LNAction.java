// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 15:30:14 PST by lamport
//      modified on Fri Sep 22 21:53:09 PDT 2000 by yuanyu

package tlc2.tool.liveness;

import tla2sany.semantic.ExprNode;
import tla2sany.semantic.LevelConstants;
import tlc2.output.EC;
import tlc2.tool.EvalControl;
import tlc2.tool.ITool;
import tlc2.tool.TLCState;
import tlc2.util.Context;
import tlc2.value.IBoolValue;
import tlc2.value.IValue;
import util.Assert;
import util.WrongInvocationException;

/**
 * Handles actions
 *
 * @author Leslie Lamport, Yuan Yu, Simon Zambrovski
 * @version $Id$
 */
public class LNAction extends LiveExprNode {
	private final ExprNode body;
	private final ExprNode subscript;
	private final boolean isBox; // <A>_v: A /\ v'!=v or [A]_v: A \/ v'=v
	private final Context con;
	private int tag;

	public LNAction(final ExprNode body, final Context con, final ExprNode subscript, final boolean isBox) {
		this.body = body;
		this.subscript = subscript;
		this.isBox = isBox;
		this.con = con;
	}

	public LNAction(final ExprNode body, final Context con) {
		this.body = body;
		this.subscript = null;
		this.isBox = false;
		this.con = con;
	}

	public final int getTag() {
		return this.tag;
	}

	public final void setTag(final int t) {
		this.tag = t;
	}

	@Override
    public final int getLevel() {
		return LevelConstants.ActionLevel;
	}

	@Override
	public final boolean containAction() {
		return true;
	}

	@Override
    public final boolean eval(final ITool tool, final TLCState s1, final TLCState s2) {
		if (this.subscript != null) {
			final IValue v1 = tool.eval(this.subscript, con, s1, TLCState.Empty, EvalControl.Clear);
			final IValue v2 = tool.eval(this.subscript, con, s2, null, EvalControl.Clear);
			final boolean isStut = v1.equals(v2);
			if (this.isBox) {
				if (isStut) {
					return true;
				}
			} else {
				if (isStut) {
					return false;
				}
			}
		}
		final IValue val = tool.eval(this.body, con, s1, s2, EvalControl.Clear);
		if (!(val instanceof IBoolValue)) {
			Assert.fail(EC.TLC_LIVE_ENCOUNTERED_NONBOOL_PREDICATE);
		}
		return ((IBoolValue) val).getVal();
	}

	@Override
    public final void toString(final StringBuffer sb, final String padding) {
		if (this.subscript == null) {
			this.body.toString(sb, padding);
		} else {
			sb.append((this.isBox) ? "[" : "<");
			this.body.toString(sb, padding + " ");
			sb.append((this.isBox) ? "]_" : ">_").append(this.subscript);
		}
	}

	@Override
    public int tagExpr(final int tag) {
		setTag(tag);
		return tag + 1;
	}

	@Override
    public LiveExprNode makeBinary() {
		// We do not deal with actions:
		throw new WrongInvocationException("LiveExprNode.makeBinary: TLC encounters actions.");
	}

	@Override
    public boolean equals(final LiveExprNode exp) {
		if (exp instanceof LNAction) {
			return getTag() == ((LNAction) exp).getTag();
		}
		return false;
	}
}
