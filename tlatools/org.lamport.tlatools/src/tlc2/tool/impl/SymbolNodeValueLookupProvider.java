package tlc2.tool.impl;

import tla2sany.semantic.APSubstInNode;
import tla2sany.semantic.ASTConstants;
import tla2sany.semantic.ExprNode;
import tla2sany.semantic.ExprOrOpArgNode;
import tla2sany.semantic.FormalParamNode;
import tla2sany.semantic.LabelNode;
import tla2sany.semantic.LetInNode;
import tla2sany.semantic.OpApplNode;
import tla2sany.semantic.OpArgNode;
import tla2sany.semantic.OpDefNode;
import tla2sany.semantic.SemanticNode;
import tla2sany.semantic.Subst;
import tla2sany.semantic.SubstInNode;
import tla2sany.semantic.SymbolNode;
import tlc2.tool.BuiltInOPs;
import tlc2.tool.ToolGlobals;
import tlc2.tool.coverage.CostModel;
import tlc2.util.Context;
import tlc2.value.impl.EvaluatingValue;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.LazyValue;
import tlc2.value.impl.MethodValue;
import util.UniqueString;

public interface SymbolNodeValueLookupProvider {
    /* Return the variable if expr is a state variable. Otherwise, null. */
	default SymbolNode getVar(final SemanticNode expr, final Context c, final boolean cutoff, final int forToolId) {
		if (expr instanceof OpApplNode odn) {
			final SymbolNode opNode = odn.getOperator();

			if (opNode.getArity() == 0) {
				final boolean isVarDecl = (opNode.getKind() == ASTConstants.VariableDeclKind);
				final Object val = lookup(opNode, c, cutoff && isVarDecl, forToolId);

				if (val instanceof final LazyValue lval) {
                    return getVar(lval.expr, lval.con, cutoff, forToolId);
				}
				if (val instanceof OpDefNode valodn) {
					return getVar(valodn.getBody(), c, cutoff, forToolId);
				}
				if (isVarDecl) {
					return opNode;
				}
			}
		}
		return null;
	}

	default Object lookup(final SymbolNode opNode, final Context c, final boolean cutoff, final int forToolId) {
		final boolean isVarDecl = (opNode.getKind() == ASTConstants.VariableDeclKind);
		Object result = c.lookup(opNode, cutoff && isVarDecl);
		if (result != null) {
			return result;
		}

		result = opNode.getToolObject(forToolId);
		if (result != null) {
			return WorkerValue.mux(result);
		}

		if (opNode.getKind() == ASTConstants.UserDefinedOpKind) {
			// Changed by LL on 10 Apr 2011 from
			//
			// result = ((OpDefNode) opNode).getBody().getToolObject(toolId);
			//
			// to the following
			ExprNode body = ((OpDefNode) opNode).getBody();
			result = body.getToolObject(forToolId);
			while ((result == null) && (body.getKind() == ASTConstants.SubstInKind)) {
				body = ((SubstInNode) body).getBody();
				result = body.getToolObject(forToolId);
			}
			// end change

			if (result != null) {
				return result;
			}
		}
		return opNode;
	}

	default Object getVal(final ExprOrOpArgNode expr, final Context c, final boolean cachable, final int forToolId) {
		return getVal(expr, c, cachable, CostModel.DO_NOT_RECORD, forToolId);
	}

	default Object getVal(final ExprOrOpArgNode expr, final Context c, final boolean cachable, final CostModel cm, final int forToolId) {
		if (expr instanceof ExprNode) {
			return new LazyValue(expr, c, cachable, cm);
		}
		final SymbolNode opNode = ((OpArgNode) expr).getOp();
		return lookup(opNode, c, false, forToolId);
	}

	default Context getOpContext(final OpDefNode opDef, final ExprOrOpArgNode[] args, final Context c, final boolean cachable, final int forToolId) {
		return getOpContext(opDef, args, c, cachable, CostModel.DO_NOT_RECORD, forToolId);
	}

	default Context getOpContext(final OpDefNode opDef, final ExprOrOpArgNode[] args, final Context c,
                                 final boolean cachable, final CostModel cm, final int forToolId) {
		final FormalParamNode[] formals = opDef.getParams();
		final int alen = args.length;
		Context c1 = c;
		for (int i = 0; i < alen; i++) {
			final Object aval = getVal(args[i], c, cachable, cm, forToolId);
			c1 = c1.cons(formals[i], aval);
		}
		return c1;
	}

    /**
     * This method only returns an approximation of the level of the
     * expression.  The "real" level is at most the return value. Adding
     * <name, ValOne> to the context means that there is no need to
     * compute level for name.
     *
     * Note that this method does not work if called on a part of an
     * EXCEPT expression.
     */
	default int getLevelBound(final SemanticNode expr, final Context c, final int forToolId) {
		switch (expr.getKind()) {
			case ASTConstants.OpApplKind -> {
				final OpApplNode expr1 = (OpApplNode) expr;
				return getLevelBoundAppl(expr1, c, forToolId);
			}
			case ASTConstants.LetInKind -> {
				final LetInNode expr1 = (LetInNode) expr;
				final OpDefNode[] letDefs = expr1.getLets();
				final int letLen = letDefs.length;
				Context c1 = c;
				int level = 0;
				for (final OpDefNode opDef : letDefs) {
					level = Math.max(level, getLevelBound(opDef.getBody(), c1, forToolId));
					c1 = c1.cons(opDef, IntValue.ValOne);
				}
				return Math.max(level, getLevelBound(expr1.getBody(), c1, forToolId));
			}
			case ASTConstants.SubstInKind -> {
				final SubstInNode expr1 = (SubstInNode) expr;
				final Subst[] subs = expr1.getSubsts();
				final int slen = subs.length;
				Context c1 = c;
				for (final Subst sub : subs) {
					c1 = c1.cons(sub.getOp(), getVal(sub.getExpr(), c, true, forToolId));
				}
				return getLevelBound(expr1.getBody(), c1, forToolId);
			}


			// Added by LL on 13 Nov 2009 to handle theorem and assumption names.
			case ASTConstants.APSubstInKind -> {
				final APSubstInNode expr1 = (APSubstInNode) expr;
				final Subst[] subs = expr1.getSubsts();
				final int slen = subs.length;
				Context c1 = c;
				for (final Subst sub : subs) {
					c1 = c1.cons(sub.getOp(), getVal(sub.getExpr(), c, true, forToolId));
				}
				return getLevelBound(expr1.getBody(), c1, forToolId);
			}


			/***********************************************************************
			 * LabelKind case added by LL on 13 Jun 2007. *
			 ***********************************************************************/
			case ASTConstants.LabelKind -> {
				final LabelNode expr1 = (LabelNode) expr;
				return getLevelBound(expr1.getBody(), c, forToolId);
			}
			default -> {
				return 0;
			}
		}
	}

	/**
	 * Users will likely want to call only {@link #getLevelBound(SemanticNode, Context, int)} - this
	 * 	method is called from that method in certain cases.
	 */
	default int getLevelBoundAppl(final OpApplNode expr, Context c, final int forToolId) {
		final SymbolNode opNode = expr.getOperator();
		final UniqueString opName = opNode.getName();
		final int opcode = BuiltInOPs.getOpCode(opName);

		if (BuiltInOPs.isTemporal(opcode)) {
			return 3; // Conservative estimate
		}

		if (BuiltInOPs.isAction(opcode)) {
			return 2; // Conservative estimate
		}

		if (opcode == ToolGlobals.OPCODE_enabled) {
			return 1; // Conservative estimate
		}

		int level = 0;
		final ExprNode[] bnds = expr.getBdedQuantBounds();
        for (final ExprNode bnd : bnds) {
            level = Math.max(level, getLevelBound(bnd, c, forToolId));
        }

		if (opcode == ToolGlobals.OPCODE_rfs) {
			// For recursive function, don't compute level of the function body
			// again in the recursive call.
			final SymbolNode fname = expr.getUnbdedQuantSymbols()[0];
			c = c.cons(fname, IntValue.ValOne);
		}

		final ExprOrOpArgNode[] args = expr.getArgs();
		final int alen = args.length;
        for (final ExprOrOpArgNode arg : args) {
            if (arg != null) {
                level = Math.max(level, getLevelBound(arg, c, forToolId));
            }
        }

		if (opcode == 0) {
			// This operator is a user-defined operator.
			if (opName.getVarLoc() >= 0)
				return 1;

			final Object val = lookup(opNode, c, false, forToolId);
			if (val instanceof final OpDefNode opDef) {
                c = c.cons(opNode, IntValue.ValOne);
				level = Math.max(level, getLevelBound(opDef.getBody(), c, forToolId));
			} else if (val instanceof final LazyValue lv) {
                level = Math.max(level, getLevelBound(lv.expr, lv.con, forToolId));
            } else if (val instanceof final EvaluatingValue ev) {
                level = Math.max(level, ev.getMinLevel());
            } else if (val instanceof final MethodValue mv) {
                level = Math.max(level, mv.getMinLevel());
			}
		}
		return level;
	}
}
