// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 19 May 2008 at  1:13:48 PST by lamport
//      modified on Fri Aug 24 14:43:24 PDT 2001 by yuanyu

package tlc2.tool.impl;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import tla2sany.modanalyzer.ParseUnit;
import tla2sany.modanalyzer.SpecObj;
import tla2sany.semantic.*;
import tlc2.TLCGlobals;
import tlc2.output.EC;
import tlc2.tool.*;
import tlc2.tool.impl.Tool.Mode;
import tlc2.util.Context;
import tlc2.util.ObjLongTable;
import tlc2.util.Vect;
import tlc2.value.ValueConstants;
import tlc2.value.impl.EvaluatingValue;
import tlc2.value.impl.LazyValue;
import tlc2.value.impl.ModelValue;
import util.Assert;
import util.FilenameToStream;
import util.MonolithSpecExtractor;
import util.UniqueString;

// Note that we use all of the {@code default} defined functionality in our
//		implemented interface {@link SymbolNodeValueLookupProvider} (and our
//		lack of implementation of {@link OpDefEvaluator} is why this class
//		is marked {@code abstract}.)
abstract class Spec
		implements ValueConstants, ToolGlobals, Serializable, OpDefEvaluator, SymbolNodeValueLookupProvider {
	private static final long serialVersionUID = 1L;

	/**
	 * @see See note on performance in CostModelCreator.
	 */
	protected static final boolean coverage = TLCGlobals.isCoverageEnabled();

	protected static final int toolId = FrontEnd.getToolId();
	
	protected final String specDir; // The spec directory.
    protected final String rootFile; // The root file of this spec.
    protected final String configFile; // The model config file.

    protected ModuleNode rootModule; // The root module.

    protected final Set<OpDefNode> processedDefs ; 
      // The set of OpDefNodes on which processSpec has been called.
      // Added by LL & YY on 25 June 2014 to eliminate infinite
      // loop when a recursively defined operator is used as an
      // operator argument in its own definition.
    protected final Defns defns; // Global definitions reachable from root
	protected final Defns unprocessedDefns;

    protected final SpecObj specObj;


    private final Vect<Action> initPred; // The initial state predicate.

    protected final Action nextPred; // The next state predicate.

    protected final Action[] temporals; // Fairness specifications...
    protected final String[] temporalNames; // ... and their names
    protected final Action[] impliedTemporals; // Liveness conds to check...
    protected final String[] impliedTemporalNames; // ... and their names
    protected final Action[] invariants; // Invariants to be checked...
    protected final String[] invNames; // ... and their names
    protected final Action[] impliedInits; // Implied-inits to be checked...
    protected final String[] impliedInitNames; // ... and their names
    protected final Action[] impliedActions; // Implied-actions to be checked...
    protected final String[] impliedActNames; // ... and their names
    protected final ExprNode[] modelConstraints; // Model constraints
    protected final ExprNode[] actionConstraints; // Action constraints
    protected final ExprNode[] assumptions; // Assumpt	ions
    protected final boolean[] assumptionIsAxiom; // assumptionIsAxiom[i] is true iff assumptions[i]
    // is an AXIOM.  Added 26 May 2010 by LL

    protected final SemanticNode viewSpec;

    protected final SemanticNode aliasSpec;

    protected final TLAClass tlaClass; // TLA built-in classes.
    private final FilenameToStream resolver; // takes care of path to stream resolution

    protected final ModelConfig config; // The model configuration.

    protected final ExternalModuleTable moduleTable;

    private final OpDeclNode[] variables;

    protected final ExprNode[] processedPostConditionSpecs;

    protected final OpDefNode counterExampleDef;

    protected final Hashtable<String, ParseUnit> parseUnitContext;

    protected final Map<ModuleNode, Map<OpDefOrDeclNode, Object>> constantDefns;

    public final TLCState EmptyState;

    // SZ Feb 20, 2009: added support to name resolver, to be able to run outside of the tool
	public Spec(final String specDir, final String specFile, final String configFile, final FilenameToStream resolver,
                final Mode mode, final Map<String, Object> params) {
        this.specDir = specDir;
        this.rootFile = specFile;
        this.defns = new Defns();
        this.tlaClass = new TLAClass("tlc2.module", resolver);
        this.processedDefs = new HashSet<>();
        this.resolver = resolver;
        
        // SZ Mar 9, 2009: added initialization of the modelValue class
        ModelValue.init();
        this.configFile = configFile;
        this.config = new ModelConfig(MonolithSpecExtractor.getConfig(configFile), resolver);
        this.config.parse();
        ModelValue.setValues(); // called after seeing all model values

        // construct new specification object, if the
        // passed one was null
        final SpecObj specObj;
        if (params.isEmpty()) {
        	specObj = new SpecObj(this.rootFile, resolver);
        } else {
        	specObj = new ParameterizedSpecObj(this, resolver, params);
        }
        var specProcessor = new SpecProcessor(getRootName(), resolver, toolId, defns, config, this, this, tlaClass, mode, specObj);

        // Parse and process this spec.
        // It takes care of all overrides.
        specProcessor.processSpec(mode);
        this.variables = specProcessor.variablesNodes;

        // set variables to the static filed in the state
        if (mode == Mode.Simulation || mode == Mode.MC_DEBUG) {
            EmptyState = TLCStateMutExt.getEmpty(this.variables);
        } else if (specProcessor.hasCallableValue) {
            assert mode == Mode.Executor;
            EmptyState = TLCStateMutExt.getEmpty(this.variables);
        } else {
            assert mode == Mode.MC;
            EmptyState = TLCStateMut.getEmpty(this.variables);
        }

        specProcessor.processSpec2();

        specProcessor.snapshot();

        specProcessor.processConstantDefns();

        // Finally, process the config file.
        specProcessor.processConfig();


        this.unprocessedDefns = specProcessor.getUnprocessedDefns();
        this.modelConstraints = specProcessor.getModelConstraints();
        this.initPred = specProcessor.getInitPred();
        this.nextPred = specProcessor.getNextPred();
        this.actionConstraints = specProcessor.getActionConstraints();
        this.rootModule = specProcessor.getRootModule();
        this.specObj = specProcessor.getSpecObj();
        this.moduleTable = specProcessor.getModuleTbl();
        this.temporals = specProcessor.getTemporal();
        this.temporalNames = specProcessor.getImpliedTemporalNames();
        this.impliedTemporals = specProcessor.getImpliedTemporals();
        this.impliedTemporalNames = specProcessor.getImpliedTemporalNames();
        this.invariants = specProcessor.getInvariants();
        this.invNames = specProcessor.getInvariantsNames();
        this.impliedInits = specProcessor.getImpliedInits();
        this.impliedInitNames = specProcessor.getImpliedInitNames();
        this.impliedActions = specProcessor.getImpliedActions();
        this.impliedActNames = specProcessor.getImpliedActionNames();
        this.assumptions = specProcessor.getAssumptions();
        var postConditionSpecs = specProcessor.getPostConditionSpecs();
        this.parseUnitContext = specProcessor.getSpecObj().parseUnitContext;
        this.assumptionIsAxiom = specProcessor.getAssumptionIsAxiom();

        this.viewSpec = generateViewSpec(this.config, this.defns);
        this.aliasSpec = generateAliasSpec(this.config, this.defns);
        this.processedPostConditionSpecs = generatePostConditionSpecs(this.config, this.defns, postConditionSpecs);
        this.counterExampleDef = generateCounterExampleDef(this.defns);


        this.constantDefns = specProcessor.getConstantDefns();
    }
    
    protected Spec(final Spec other) {
    	this.specDir = other.specDir;
    	this.rootFile = other.rootFile;
    	this.configFile = other.configFile;
    	this.processedDefs = other.processedDefs;
    	this.defns = other.defns;
    	this.tlaClass = other.tlaClass;
        this.resolver = other.resolver;
        this.unprocessedDefns = other.unprocessedDefns;
    	this.config = other.config;
        this.variables = other.variables;
        this.modelConstraints = other.modelConstraints;
        this.initPred = other.initPred;
        this.nextPred = other.nextPred;
        this.actionConstraints = other.actionConstraints;
        this.rootModule = other.rootModule;
        this.specObj = other.specObj;
        this.moduleTable = other.moduleTable;
        this.temporals = other.temporals;
        this.temporalNames = other.temporalNames;
        this.impliedTemporals = other.impliedTemporals;
        this.impliedTemporalNames = other.impliedTemporalNames;
        this.impliedInits = other.impliedInits;
        this.impliedInitNames = other.impliedInitNames;
        this.invariants = other.invariants;
        this.invNames = other.invNames;
        this.impliedActions = other.impliedActions;
        this.impliedActNames = other.impliedActNames;
        this.assumptions = other.assumptions;
        this.processedPostConditionSpecs = other.processedPostConditionSpecs;
        this.counterExampleDef = other.counterExampleDef;

        this.parseUnitContext = other.parseUnitContext;
        this.assumptionIsAxiom = other.assumptionIsAxiom;

        this.aliasSpec = other.aliasSpec;
        this.viewSpec = other.viewSpec;

        this.constantDefns = other.constantDefns;
        this.EmptyState = other.EmptyState;
    }

    public OpDeclNode[] getVariables(){
        return variables;
    }

    public TLCState getEmptyState(){
        return this.EmptyState;
    }

    public TLCState createEmptyState() {
        return this.EmptyState.createEmpty();
    }

    public ModelConfig getModelConfig() {
    	return config;
    }

    /* Return the variable if expr is a primed state variable. Otherwise, null. */
    public final SymbolNode getPrimedVar(final SemanticNode expr, final Context c, final boolean cutoff)
    {
        if (expr instanceof final OpApplNode expr1)
        {
            final SymbolNode opNode = expr1.getOperator();

            if (BuiltInOPs.getOpCode(opNode.getName()) == OPCODE_prime)
            {
                return this.getVar(expr1.getArgs()[0], c, cutoff, toolId);
            }

            if (opNode.getArity() == 0)
            {
                final boolean isVarDecl = (opNode.getKind() == VariableDeclKind);
                final Object val = this.lookup(opNode, c, cutoff && isVarDecl, toolId);

                if (val instanceof final LazyValue lval)
                {
                    return this.getPrimedVar(lval.expr, lval.con, cutoff);
                }
                if (val instanceof OpDefNode odn)
                {
                    return this.getPrimedVar(odn.getBody(), c, cutoff);
                }
            }
        }
        return null;
    }

    public ModuleNode getRootModule() {
        return rootModule;
    }
    public final Map<ModuleNode, Map<OpDefOrDeclNode, Object>> getConstantDefns() {
        return constantDefns;
    }

    public Defns getDefns() {
        return defns;
    }

    public Action getNextPred() {
        return nextPred;
    }

    /** 
     * Get model constraints.  
     */
	public final ExprNode[] getModelConstraints() {
		return modelConstraints;
	}

    /**
     * Get action constraints.  
     */
	public final ExprNode[] getActionConstraints() {
		return actionConstraints;
	}

    /* Get the initial state predicate of the specification.  */
	public final Vect<Action> getInitStateSpec() {
		return initPred;
	}

    /* Get the action (next state) predicate of the specification. */
	public final Action getNextStateSpec() {
		return nextPred;
	}

    private static SemanticNode generateViewSpec(ModelConfig config, Defns defns) {
        final String name = config.getView();
        if (name.length() == 0)
            return null;

        final Object view = defns.get(name);
        if (view == null)
        {
            Assert.fail(EC.TLC_CONFIG_SPECIFIED_NOT_DEFINED, new String[] { "view function", name });
        }

        if (!(view instanceof OpDefNode))
        {
            Assert.fail(EC.TLC_CONFIG_ID_MUST_NOT_BE_CONSTANT, new String[] { "view function", name });
        }
        final OpDefNode def = (OpDefNode) view;
        if (def.getArity() != 0)
        {
            Assert.fail(EC.TLC_CONFIG_ID_REQUIRES_NO_ARG, new String[] { "view function", name });
        }
        return def.getBody();
    }

    /** 
     * Get the view mapping for the specification. 
     */
    public final SemanticNode getViewSpec()
    {
        return viewSpec;
    }

    public static SemanticNode generateAliasSpec(ModelConfig config, Defns defns){
        final String name = config.getAlias();
        if (name.length() == 0)
        {
            return null;//Assert.fail(EC.TLC_CONFIG_NO_STATE_TYPE);
        }

        // A true constant-level alias such as such as [ x |-> "foo" ] will be evaluated
        // eagerly and type be an instance of RecordValue.  It would be good to return a
        // proper warning.
        final Object type = defns.get(name);
        if (type == null)
        {
            Assert.fail(EC.TLC_CONFIG_SPECIFIED_NOT_DEFINED, new String[] { "alias", name });
        }
        if (!(type instanceof OpDefNode))
        {
            Assert.fail(EC.TLC_CONFIG_ID_MUST_NOT_BE_CONSTANT, new String[] { "alias", name });
        }
        final OpDefNode def = (OpDefNode) type;
        if (def.getArity() != 0)
        {
            Assert.fail(EC.TLC_CONFIG_ID_REQUIRES_NO_ARG, new String[] { "alias", name });
        }
        return def.getBody();
    }

    /* Get the alias declaration for the state variables. */
    public final SemanticNode getAliasSpec()
    {
        return aliasSpec;
    }

    private static ExprNode[] generatePostConditionSpecs(ModelConfig config, Defns defns, List<ExprNode> res){

        final String name = config.getPostCondition();
        if (name.length() != 0)
        {
            final Object type = defns.get(name);
            if (type == null)
            {
                Assert.fail(EC.TLC_CONFIG_SPECIFIED_NOT_DEFINED, new String[] { "post condition", name });
            }
            if (!(type instanceof OpDefNode))
            {
                Assert.fail(EC.TLC_CONFIG_ID_MUST_NOT_BE_CONSTANT, new String[] { "post condition", name });
            }
            final OpDefNode def = (OpDefNode) type;
            if (def.getArity() != 0)
            {
                Assert.fail(EC.TLC_CONFIG_ID_REQUIRES_NO_ARG, new String[] { "post condition", name });

            }
            res.add(def.getBody());
        }

        return res.toArray(ExprNode[]::new);
    }

    public final ExprNode[] getPostConditionSpecs()
    {
    	return this.processedPostConditionSpecs;
    }

    private static OpDefNode generateCounterExampleDef(Defns defns) {
        // Defined in TLCExt.tla
        final Object type = defns.get("CounterExample");
        if (type == null)
        {
            // Not used anywhere in the current spec.
            return null;
        }
        if (!(type instanceof EvaluatingValue))
        {
            Assert.fail(EC.GENERAL);
        }
        final OpDefNode def = ((EvaluatingValue) type).getOpDef();
        if (def.getArity() != 0)
        {
            Assert.fail(EC.GENERAL);
        }
        return def;
    }

    public final OpDefNode getCounterExampleDef()
    {
    	return this.counterExampleDef;
    }

    public ExternalModuleTable getModuleTbl() {
        return moduleTable;
    }

	public final boolean livenessIsTrue() {
		return getImpliedTemporals().length == 0;
	}

    /* Get the fairness condition of the specification.  */
    public final Action[] getTemporals()
    {
        return temporals;
    }

    public Vect<Action> getInitPred() {
        return initPred;
    }

    public final String[] getTemporalNames()
    {
        return temporalNames;
    }

    /* Get the liveness checks of the specification.  */
	public final Action[] getImpliedTemporals() {
		return impliedTemporals;
	}

	public final String[] getImpliedTemporalNames() {
		return impliedTemporalNames;
	}

    /* Get the invariants of the specification. */
	public final Action[] getInvariants() {
		return invariants;
	}

	public final String[] getInvNames() {
		return invNames;
	}

    /* Get the implied-inits of the specification. */
	public final Action[] getImpliedInits() {
		return impliedInits;
	}

	public final String[] getImpliedInitNames() {
		return impliedInitNames;
	}

    /* Get the implied-actions of the specification. */
	public final Action[] getImpliedActions() {
		return impliedActions;
	}

	public final String[] getImpliedActNames() {
		return impliedActNames;
	}

    /* Get the assumptions of the specification. */
	public final ExprNode[] getAssumptions() {
		return assumptions;
	}
    
    /* Get the assumptionIsAxiom field */
    public final boolean[] getAssumptionIsAxiom() {
        return assumptionIsAxiom;
    }
    
    /**
     * This method gets the value of a symbol from the environment. We
     * look up in the context c, its tool object, and the state s.
     * 
     * It and the lookup method that follows it were modified by LL
     * on 10 April 2011 to fix the following bug.  When a constant definition
     *    Foo == ...
     * is overridden to substitute Bar for Foo, the TLC tool object for
     * the body of Foo's OpDef node is set to the OpDefNode for Bar.
     * When evaluating a use of Foo, the lookup method is apparently
     * supposed to return the OpDefNode for Bar.  (I don't understand
     * how the callers make use of the returned value.) That's what it
     * does for uses of Foo in the module in which Foo is defined.
     * However, if Foo is imported by instantiation with renaming as 
     * X!Foo, then it appears that looking up X!Foo should also return 
     * the OpDefNode for Bar.  If the instantiated module had no
     * parameters, then that's what happened because the body of the
     * OpDefNode for X!Foo is the same (contains a pointer to the
     * same object) as the body of Foo's OpDefNode.  However, that
     * wasn't the case if the instantiated module had parameters,
     * because then X!Foo's OpDefNode consists of a sequence of
     * nested SubstInNode objects, the last of which points to
     * the body of Foo's OpDefNode.  So, LL modified the lookup
     * methods so they follow the sequence of SubstInNode bodies
     * down to the body of Foo's OpDefNode when looking up the result.  
     * (If a SubstInNode has a non-null TLC tool object for a
     * SubstInNode, then it returns that object.  I don't think this 
     * should ever be the case, and if it is, I have no idea what the
     * lookup method should do.)
     * 
     */
    public final Object lookup(final SymbolNode opNode, final Context c, final TLCState s, final boolean cutoff)
    {
        Object result = lookup(opNode, c, cutoff, toolId);
        if (result != opNode) {
            return result;
        }

		// CalvinL/LL/MAK 02/2021: Added conditional as part of Github issue #362 Name
		// clash between variable in refined spec and operator in instantiated spec. See
		// releated test in Github362.java.
        if (opNode.getKind() != UserDefinedOpKind) {
			result = s.lookup(opNode.getName());
			if (result != null) {
				return result;
			}
		}

        return opNode;
    }

    public final Object lookup(final SymbolNode opNode)
    {
        return lookup(opNode, Context.Empty, false, toolId);
    }

    /**
     * The following added by LL on 23 October 2012 to fix bug in evaluation of names of theorems and 
     * assumptions imported by parameterized instantiation.
     *  
     * @param opDef
     * @param args
     * @param c
     * @param cachable
     * @return
     */
    public final Context getOpContext(final ThmOrAssumpDefNode opDef, final ExprOrOpArgNode[] args, final Context c, final boolean cachable)
    {
        final FormalParamNode[] formals = opDef.getParams();
        final int alen = args.length;
        Context c1 = c;
        for (int i = 0; i < alen; i++)
        {
            final Object aval = this.getVal(args[i], c, cachable, toolId);
            c1 = c1.cons(formals[i], aval);
        }
        return c1;
    }
    
    /**
     * Return a table containing the locations of subexpression in the
     * spec of forms x' = e and x' \in e. Warning: Current implementation
     * may not be able to find all such locations.
     */
    public final ObjLongTable<SemanticNode> getPrimedLocs()
    {
        final ObjLongTable<SemanticNode> tbl = new ObjLongTable<>(10);
        final Action act = this.getNextStateSpec();
		if (act == null) {
			// MAK 10/17/2018: If spec defines no next-state action (see e.g.
			// tlc2.tool.ASTest) and this method is called before ModelChecker checks
			// actions (search for tlc2.output.EC.TLC_STATES_AND_NO_NEXT_ACTION) this will
			// NPE.
			return tbl;
		}
        this.collectPrimedLocs(act.pred, act.con, tbl);
        return tbl;
    }

    public final void collectPrimedLocs(final SemanticNode pred, final Context c, final ObjLongTable<SemanticNode> tbl)
    {
        switch (pred.getKind()) {
        case OpApplKind: {
            final OpApplNode pred1 = (OpApplNode) pred;
            this.collectPrimedLocsAppl(pred1, c, tbl);
            return;
        }
        case LetInKind: {
            final LetInNode pred1 = (LetInNode) pred;
            this.collectPrimedLocs(pred1.getBody(), c, tbl);
            return;
        }
        case SubstInKind: {
            final SubstInNode pred1 = (SubstInNode) pred;
            final Subst[] subs = pred1.getSubsts();
            Context c1 = c;
            for (final Subst sub : subs) {
                c1 = c1.cons(sub.getOp(), this.getVal(sub.getExpr(), c, true, toolId));
            }
            this.collectPrimedLocs(pred1.getBody(), c, tbl);
            return;
        }

        // Added by LL on 13 Nov 2009 to handle theorem and assumption names.
        case APSubstInKind: {
            final APSubstInNode pred1 = (APSubstInNode) pred;
            final Subst[] subs = pred1.getSubsts();
            Context c1 = c;
            for (final Subst sub : subs) {
                c1 = c1.cons(sub.getOp(), this.getVal(sub.getExpr(), c, true, toolId));
            }
            this.collectPrimedLocs(pred1.getBody(), c, tbl);
            return;
        }


            /***********************************************************************
            * LabelKind case added by LL on 13 Jun 2007.                           *
            ***********************************************************************/
        case LabelKind: {
            final LabelNode pred1 = (LabelNode) pred;
            this.collectPrimedLocs(pred1.getBody(), c, tbl);
        }
        }
    }

    private final void collectPrimedLocsAppl(final OpApplNode pred, final Context c, final ObjLongTable<SemanticNode> tbl)
    {
        final ExprOrOpArgNode[] args = pred.getArgs();
        final SymbolNode opNode = pred.getOperator();
        final int opcode = BuiltInOPs.getOpCode(opNode.getName());

        switch (opcode) {
        case OPCODE_fa: // FcnApply
            case OPCODE_aa: // AngleAct <A>_e
            {
            this.collectPrimedLocs(args[0], c, tbl);
            break;
        }
        case OPCODE_ite: // IfThenElse
        {
            this.collectPrimedLocs(args[1], c, tbl);
            this.collectPrimedLocs(args[2], c, tbl);
            break;
        }
        case OPCODE_case: // Case
        {
            for (final ExprOrOpArgNode arg : args) {
                final OpApplNode pair = (OpApplNode) arg;
                this.collectPrimedLocs(pair.getArgs()[1], c, tbl);
            }
            break;
        }
        case OPCODE_eq:   // x' = 42
        case OPCODE_in: { // x' \in S (eq case "falls through")
            final SymbolNode var = this.getPrimedVar(args[0], c, false);
            if (var != null && var.getName().getVarLoc() != -1)
            {
                tbl.put(pred, 0);
            }
            break;
        }
        case OPCODE_cl: // ConjList
        case OPCODE_dl: // DisjList
        case OPCODE_be: // BoundedExists
        case OPCODE_bf: // BoundedForall
        case OPCODE_land:
        case OPCODE_lor:
        case OPCODE_implies:
        case OPCODE_nop: // This case added 13 Nov 2009 by LL to handle subexpression names.
          {
              for (final ExprOrOpArgNode arg : args) {
                  this.collectPrimedLocs(arg, c, tbl);
              }
            break;
        }
        case OPCODE_unchanged: {
            this.collectUnchangedLocs(args[0], c, tbl);
            break;
        }
            case OPCODE_sa: // [A]_e
        {
            this.collectPrimedLocs(args[0], c, tbl);
            tbl.put(args[1], 0);
            break;
        }
        default: {
            if (opcode == 0)
            {
                final Object val = this.lookup(opNode, c, false, toolId);

                if (val instanceof final OpDefNode opDef)
                {
                    // Following added by LL on 10 Apr 2010 to avoid infinite
                    // recursion for recursive operator definitions
                    if (opDef.getInRecursive()) {
                        return ;
                    }
                    final Context c1 = this.getOpContext(opDef, args, c, true, toolId);
                    this.collectPrimedLocs(opDef.getBody(), c1, tbl);
                } else if (val instanceof final LazyValue lv)
                {
                    this.collectPrimedLocs(lv.expr, lv.con, tbl);
                }
            }
        }
        }
    }

	private final void collectUnchangedLocs(final SemanticNode expr, final Context c,
			final ObjLongTable<SemanticNode> tbl) {
        if (expr instanceof final OpApplNode expr1)
        {
            final SymbolNode opNode = expr1.getOperator();
            final UniqueString opName = opNode.getName();
            final int opcode = BuiltInOPs.getOpCode(opName);

            if (opName.getVarLoc() >= 0)
            {
                // a state variable:
                tbl.put(expr, 0);
                return;
            }

            final ExprOrOpArgNode[] args = expr1.getArgs();
            if (opcode == OPCODE_tup)
            {
				// a tuple, might be:
            	// UNCHANGED <<x,y,z>>
            	// or:
            	// vars == <<x,y,z>>
            	// ...
            	// UNCHANGED vars
				// For the latter, we don't want vars == <<x,y,z>> to show up, but the vars in
				// UNCHANGED vars (see CoverageStatisticsTest).
                for (final ExprOrOpArgNode arg : args) {
                    this.collectUnchangedLocs(arg, c, tbl);
                }
                return;
            }

            if (opcode == 0 && args.length == 0)
            {
                // a 0-arity operator:
                final Object val = this.lookup(opNode, c, false, toolId);
                if (val instanceof OpDefNode odn)
                {
                    this.collectUnchangedLocs(odn.getBody(), c, tbl);
                }
            }
        }
    }

    public FilenameToStream getResolver()
    {
        return resolver;
    }
    
    public String getRootName() {
    	return new File(this.rootFile).getName();
    }
    
    public String getRootFile() {
    	return this.rootFile;
    }

    public String getConfigFile() {
    	return this.configFile;
    }
    
    public String getSpecDir() {
    	return this.specDir;
    }
    
    public int getId() {
    	return toolId;
    }

	public ModuleNode getModule(final String moduleName) {
		return moduleTable.getModuleNode(moduleName);
	}

	public List<File> getModuleFiles(final FilenameToStream resolver) {
		final List<File> result = new ArrayList<>();
	
		final Enumeration<ParseUnit> parseUnitContextElements = parseUnitContext.elements();
		while (parseUnitContextElements.hasMoreElements()) {
			final ParseUnit pu = parseUnitContextElements.nextElement();
			final File resolve = resolver.resolve(pu.getFileName(), false);
			result.add(resolve);
		}
		return result;
	}
}
