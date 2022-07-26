/*******************************************************************************
 * Copyright (c) 2021 Microsoft Research. All rights reserved.
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package tlc2.module;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import tla2sany.semantic.ExprOrOpArgNode;
import tlc2.TLCGlobals;
import tlc2.output.EC;
import tlc2.overrides.Evaluation;
import tlc2.overrides.TLAPlusOperator;
import tlc2.tool.*;
import tlc2.tool.coverage.CostModel;
import tlc2.tool.impl.Tool;
import tlc2.util.Context;
import tlc2.util.IdThread;
import tlc2.util.Vect;
import tlc2.value.ValueConstants;
import tlc2.value.Values;
import tlc2.value.impl.BoolValue;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.RecordValue;
import tlc2.value.impl.SetEnumValue;
import tlc2.value.impl.StringValue;
import tlc2.value.impl.Value;
import tlc2.value.impl.ValueVec;
import util.ToolIO;
import util.UniqueString;

public class TLCGetSet implements ValueConstants {

	public static Value narrowToIntValue(final long value) {
        if ((int)value != value) {
        	return IntValue.ValNegOne;
        }
        return IntValue.gen((int) value);
	}

	// TLCSet(..)
	private static final UniqueString EXIT = UniqueString.uniqueStringOf("exit");
	private static final UniqueString PAUSE = UniqueString.uniqueStringOf("pause");
	
	// TLCGet(..)
	private static final UniqueString CONFIG = UniqueString.uniqueStringOf("config");
	private static final UniqueString SPEC = UniqueString.uniqueStringOf("spec");
	private static final UniqueString ACTION = UniqueString.uniqueStringOf("action");
	public static final UniqueString INSTALL = UniqueString.uniqueStringOf("install");

	public static final UniqueString BEHAVIOR = UniqueString.of("behavior");
	public static final UniqueString ALL_VALUES = UniqueString.of("all");
	
	public static final UniqueString MODE = UniqueString.uniqueStringOf("mode");
	public static final UniqueString DEADLOCK = UniqueString.uniqueStringOf("deadlock");
	public static final UniqueString SEED = UniqueString.uniqueStringOf("seed");
	public static final UniqueString FINGERPRINT = UniqueString.uniqueStringOf("fingerprint");
	public static final UniqueString WORKER = UniqueString.uniqueStringOf("worker");
	public static final UniqueString TRACES = UniqueString.uniqueStringOf("traces");
	public static final UniqueString DEPTH = UniqueString.uniqueStringOf("depth");
	public static final UniqueString ARIL = UniqueString.uniqueStringOf("aril");
	
	public static final UniqueString REVISION = UniqueString.uniqueStringOf("revision");
	public static final UniqueString REV_TIMESTAMP = UniqueString.uniqueStringOf("timestamp");
	public static final UniqueString REV_DATE = UniqueString.uniqueStringOf("date");
	public static final UniqueString REV_COUNT = UniqueString.uniqueStringOf("count");
	public static final UniqueString REV_TAG = UniqueString.uniqueStringOf("tag");
	
	private static final UniqueString SPEC_IMPLIEDINITS = UniqueString.of("impliedinits");
	private static final UniqueString SPEC_INVARIANTS = UniqueString.of("invariants");
	private static final UniqueString SPEC_IMPLIEDTEMPORALS = UniqueString.of("impliedtemporals");
	private static final UniqueString SPEC_TERMPORALS = UniqueString.of("temporals");
	public static final UniqueString SPEC_ACTIONS = UniqueString.of("actions");
	private static final UniqueString SPEC_INITS = UniqueString.of("inits");

	// TLCGet(..)
	// BFS & Simulation mode
	// Considered to be part of "statistics", but it is a property of the current behavior.
	public static final UniqueString LEVEL = UniqueString.uniqueStringOf("level");

	// TLCGet("stats")
	// Wrapper for all the other named registers below, except that "stats"
	// works for both BFS and simulation whereas some of the named registers below
	// didn't work for both modes.  Now, a user can safely do:
	//     DOMAIN TLCGet("stats").
	private static final UniqueString STATISTICS = UniqueString.uniqueStringOf("stats");

	public static final UniqueString DURATION = UniqueString.uniqueStringOf("duration");
	// BFS: The number of generated states.
	// Simulation: The total number of states generated.
	public static final UniqueString GENERATED = UniqueString.uniqueStringOf("generated");

	// BFS: The length of the longest behavior generated so far.
	// Simulation: The number of traces generated by the current worker. (since July
	// 2020 in commit 557c674c0f314c2e70885a4d5994e3e858bab64a). This should be removed
	// eventually because "diameter" was hijacked for simulation.
	public static final UniqueString DIAMETER = UniqueString.uniqueStringOf("diameter");
	
	// BFS: The number of distinct states.
	public static final UniqueString DISTINCT = UniqueString.uniqueStringOf("distinct");
	// BFS: The number of unexplored distinct states.
	public static final UniqueString QUEUE = UniqueString.uniqueStringOf("queue");

	
	public static final long serialVersionUID = 20210330L;

	private static final long startTime = System.currentTimeMillis();

	@Evaluation(definition = "TLCGet", module = "TLC", warn = false, silent = true, minLevel = 1)
	public static Value TLCGetEval(final Tool tool, final ExprOrOpArgNode[] args, final Context c, final TLCState s0,
			final TLCState s1, final int control, final CostModel cm) {

		var mainTool = IdThread.getTool();


		AbstractChecker mainChecker = null;
		Simulator simulator = null;

		if (Objects.nonNull(mainTool)){
			mainChecker = mainTool.getMainChecker();
			simulator = mainTool.getSimulator();
		}

		final Value vidx = tool.eval(args[0], c, s0, s1, control, cm);
		if (vidx instanceof IntValue iv) {
			final int idx = iv.val;
			if (idx >= 0) {
				final Thread th = Thread.currentThread();
				Value res = null;
				if (th instanceof IdThread idT) {
					res = (Value) idT.getLocalValue(idx);
				} else if (mainChecker != null) {
					res = (Value)mainChecker.getValue(0, idx);
				} else if (simulator != null) {
					res = (Value) simulator.getLocalValue(idx);
				}
				if (res == null) {
					throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(idx));
				}
				return res;
			}
		} else if (vidx instanceof StringValue) {
			return TLCGetStringValue(tool, vidx, s0, s1, control);
		}
		throw new EvalException(EC.TLC_MODULE_ONE_ARGUMENT_ERROR,
				new String[] { "TLCGet", "nonnegative integer", Values.ppr(vidx.toString()) });
	}

	private static final Value TLCGetStringValue(final Tool tool, final Value vidx, final TLCState s0, final TLCState s1,
			final int control) {

		final StringValue sv = (StringValue) vidx;
		var mainTool = IdThread.getTool();


		AbstractChecker mainChecker = null;
		Simulator simulator = null;

		if (Objects.nonNull(mainTool)){
			mainChecker = mainTool.getMainChecker();
			simulator = mainTool.getSimulator();
		}

		if (DIAMETER == sv.val) {
			try {
				if (mainChecker != null) {
					return IntValue.gen(mainChecker.getProgress());
				} else if (simulator != null) {
					if (Thread.currentThread() instanceof final SimulationWorker sw) {
						// non-initial states.
                        final long traceCnt = sw.getTraceCnt();
						if (traceCnt > Integer.MAX_VALUE) {
							return IntValue.gen(Integer.MAX_VALUE);
						}
						return IntValue.gen((int) traceCnt);
					} else {
						// Called while evaluating the initial predicate/generating initial states.
						return IntValue.gen(0);
					}
				} else {
					throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
				}
			} catch (final ArithmeticException e) {
				throw new EvalException(EC.TLC_MODULE_OVERFLOW, Long.toString(Objects.requireNonNull(mainChecker).getProgress()));
			} catch (final NullPointerException npe) {
				// tool.getMainChecker() is null while the spec is parsed. A constant
				// expression referencing one of the named values here would thus result in an
				// NPE.
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (GENERATED == sv.val) {
			try {
				return IntValue.gen(Math.toIntExact(mainChecker.getStatesGenerated()));
			} catch (final ArithmeticException e) {
				throw new EvalException(EC.TLC_MODULE_OVERFLOW,
						Long.toString(mainChecker.getStatesGenerated()));
			} catch (final NullPointerException npe) {
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (DISTINCT == sv.val) {
			try {
				return IntValue.gen(Math.toIntExact(mainChecker.getDistinctStatesGenerated()));
			} catch (final ArithmeticException e) {
				throw new EvalException(EC.TLC_MODULE_OVERFLOW,
						Long.toString(mainChecker.getDistinctStatesGenerated()));
			} catch (final NullPointerException npe) {
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (QUEUE == sv.val) {
			try {
				return IntValue.gen(Math.toIntExact(mainChecker.getStateQueueSize()));
			} catch (final ArithmeticException e) {
				throw new EvalException(EC.TLC_MODULE_OVERFLOW,
						Long.toString(mainChecker.getStateQueueSize()));
			} catch (final NullPointerException npe) {
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (DURATION == sv.val) {
			try {
				final int duration = (int) ((System.currentTimeMillis() - startTime) / 1000L);
				return IntValue.gen(Math.toIntExact(duration));
			} catch (final ArithmeticException e) {
				throw new EvalException(EC.TLC_MODULE_OVERFLOW,
						Long.toString(((System.currentTimeMillis() - startTime) / 1000L)));
			}
		} else if (STATISTICS == sv.val) {
			try {
				if (mainChecker != null) {
					return mainChecker.getStatistics();
				} else if (simulator != null) {
					return simulator.getStatistics();
				}
			} catch (final NullPointerException npe) {
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (CONFIG == sv.val) {
			/*
			 * Add operator `TLC!TLCGet("config")`.
			 * 
				```tla
				TLC!TLCGet("config") ==
				    CHOOSE cfg \in
				       [ mode: STRING,
				          depth : Nat, trace : Nat ]
				```
			 * 
			 * Note that `TLCGet("config")` remains undocumented in `TLC.tla` until we have
			 * more confidence in its usefulness.
			 * 
			 * TODO: Initialize the RecordValue (config) eagerly to minimize the runtime
			 *       overhead.
			 */
			try {
				if (mainChecker != null) {
					return mainChecker.getConfig();
				} else if (simulator != null) {
					return simulator.getConfig();
				}
			} catch (final NullPointerException npe) {
				throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
			}
		} else if (REVISION == sv.val) {
			/*
			 * Add operator `TLC!TLCGet("revision")`.
			 */
			final UniqueString[] n = new UniqueString[4];
			final Value[] v = new Value[n.length];
			
			n[0] = TLCGetSet.REV_COUNT;
			v[0] = IntValue.gen(TLCGlobals.getSCMCommits());
			
			final Date buildDate = TLCGlobals.getBuildDate();
			// This suffers from the year 2038 problem
			// (https://en.wikipedia.org/wiki/Year_2038_problem). By then, somebody please
			// properly implement support for TLC's tlc2.util.BigInt.
			n[1] = TLCGetSet.REV_TIMESTAMP;
			v[1] = IntValue.gen((int) buildDate.toInstant().getEpochSecond());

			n[2] = TLCGetSet.REV_DATE;
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			v[2] = new StringValue(df.format(buildDate));
			
			n[3] = TLCGetSet.REV_TAG;
			v[3] = new StringValue(TLCGlobals.getRevisionOrDev());

			return new RecordValue(n, v, false);
		} else if (SPEC == sv.val) {
			/*
			 * Add operator `TLC!TLCGet("spec")`.
			 */
			final UniqueString[] n = new UniqueString[6];
			final Value[] v = new Value[n.length];

			// Inits as found by spec processing.
			final List<Value> l = new ArrayList<>();
			final Vect<Action> inits = tool.getInitStateSpec();
			for (int i = 0; i < inits.size(); i++) {
				l.add(new RecordValue(inits.elementAt(i)));
			}
			n[0] = SPEC_INITS;
			v[0] = new SetEnumValue(new ValueVec(l), false);
			
			// Actions as found by spec processing. For a sub-action with non-zero arity,
			// TLC has multiple copies.
			n[1] = SPEC_ACTIONS;
			v[1] = new SetEnumValue(new ValueVec(Arrays.stream(tool.getActions())
					.map(RecordValue::new).collect(Collectors.toList())), false);

			n[2] = SPEC_TERMPORALS;
			v[2] = new SetEnumValue(new ValueVec(Arrays.stream(tool.getTemporals())
					.map(RecordValue::new).collect(Collectors.toList())), false);
			
			n[3] = SPEC_INVARIANTS;
			v[3] = new SetEnumValue(new ValueVec(Arrays.stream(tool.getInvariants())
					.filter(a -> !a.isInternal()).map(RecordValue::new).collect(Collectors.toList())), false);
			
			n[4] = SPEC_IMPLIEDINITS;
			v[4] = new SetEnumValue(new ValueVec(Arrays.stream(tool.getImpliedInits())
					.map(RecordValue::new).collect(Collectors.toList())), false);
			
			n[5] = SPEC_IMPLIEDTEMPORALS;
			v[5] = new SetEnumValue(new ValueVec(Arrays.stream(tool.getImpliedTemporals())
					.map(RecordValue::new).collect(Collectors.toList())), false);
			
			return new RecordValue(n, v, false);
		} else if (LEVEL == sv.val) {
			// Contrary to "diameter", "level" is not monotonically increasing. "diameter"
			// is monotonically increasing because it calls tlc2.tool.TLCTrace.getLevelForReporting().
			// "level" is the height stored as part of the state that is currently explored.
			
			// Note that s1 can be null (TLCState#Null) if TLCGet("level") is primed
			// `TLCGet("level")'` and evaluated as part of the behavior spec. Related, it is
			// unclear as to why Tool#evalApplImpl(..) does *not* set control to EV#Primed
			// for opcode prime. s0#uid might or might not be TLCState.INIT_UID, depending
			// on whether the state has already been written to disk, which happens *after*
			// state- and action-constraints are checked, but before invariants, implied
			// actions, and liveness are checked.
			
			if (EvalControl.isConst(control) || EvalControl.isInit(control)) {
				// By definition, level is 0 in ASSUME/POSTCONDITION and the initial predicate.
				return IntValue.gen(TLCState.INIT_LEVEL - 1);
			}
			return IntValue.gen(s0.getLevel());
		} else if (ACTION == sv.val) {
				/*
			    Add operator `TLC!TLCGet("action")`.
				
				```tla
				TLC!TLCGet("action") ==
				  LET LOCATIONS ==
				       [ beginLine: Nat,
				         beginColumn: Nat,
				         endLine: Nat,
				         endColumn: Nat,
				         module: STRING ]
				      CONTEXTS ==
				        [
				          ...
				        ]
				  IN CHOOSE act \in
				       [ name: STRING,
				         location : LOCATIONS,
				         context: CONTEXTS ]: TRUE
				```
				
				If `TLCGet("action")` is evaluated outside the scope of a TLA+ action of
				a behavior spec or if the action is unknown for technical reasons, the
				records (name/location) are set to dummy values.
				
				For `TLCGet("action")` to return non-dummy values, TLC internally has to
				use its extended state implementation (`tlc2.tool.TLCStateMutExt.java`).
				For now, this is the case when TLC runs with "-debugger" or "-simulate".
				
				Note that `TLCGet("action")` remains undocumented in `TLC.tla` until we
				have more confidence in its usefulness.
			 */
			if (s0 == null || s0.getAction() == null) {
				return new RecordValue(Action.UNKNOWN, Context.Empty);
			} else {
				return new RecordValue(s0.getAction(), s0.getAction().con);
			}
		} else if (ALL_VALUES == sv.val) {
			/*
			 * - Let  W  be the set  1..TLCGet("config").worker
             * - Let  Eval(w, Op)  be an operator that evaluates the given operator  Op  
             *   in the context of the w-th worker  s.t.  w \in W  .
             * - Let  I  be the set of (naturals)  i  that appear in all  TLCSet(i, v)
             *   throughout a spec.
             * 
             * TLCGet("all") ==
             *    [ i \in I |-> 
             *       [ w \in W |-> 
             *         Eval(w, TLCGet(i) ] ]
			 */
			if (mainChecker != null) {
				return mainChecker.getAllValues();
			} else if (simulator != null) {
				return simulator.getAllValues();
			}
		}
		throw new EvalException(EC.TLC_MODULE_TLCGET_UNDEFINED, String.valueOf(sv.val));
	}

	@TLAPlusOperator(identifier = "TLCSet", module = "TLC", warn = false)
	public static Value TLCSet(final Value vidx, final Value val) {
		var mainTool = IdThread.getTool();


		AbstractChecker mainChecker = null;
		Simulator simulator = null;

		if (Objects.nonNull(mainTool)){
			mainChecker = mainTool.getMainChecker();
			simulator = mainTool.getSimulator();
		}

		if (vidx instanceof IntValue iv) {
			final int idx = iv.val;
			if (idx >= 0) {
				final Thread th = Thread.currentThread();
				if (th instanceof IdThread idT) {
					idT.setLocalValue(idx, val);
				} else if (mainChecker != null) {
					mainChecker.setAllValues(idx, val);
				} else {
					simulator.setAllValues(idx, val);
				}
				return BoolValue.ValTrue;
			}
		} else if (vidx instanceof final StringValue sv) {
            if (EXIT == sv.val) {
				if (val == BoolValue.ValTrue) {

					if (mainChecker != null) {
						mainChecker.stop();
					}
					if (simulator != null) {
						simulator.stop();
					}
				}
				return BoolValue.ValTrue;
			} else if (PAUSE == sv.val) {
				// Provisional TLCSet("pause", TRUE) implementation that suspends BFS model
				// checking until enter is pressed on system.in. Either use in spec as:
				// TLCSet("pause", guard)
				// but it might be better guarded by IfThenElse for performance reasons:
				// IF guard THEN TLCSet("pause", TRUE) ELSE TRUE
				if (val == BoolValue.ValTrue && mainChecker instanceof final ModelChecker mc) {
                    synchronized (mc.theStateQueue) {
						ToolIO.out.println("Press enter to resume model checking.");
						ToolIO.out.flush();
						try {
							System.in.read();
						} catch (final IOException e) {
							throw new EvalException(EC.GENERAL, e.getMessage());
						}
					}
				}
				return BoolValue.ValTrue;
			}
		}
		throw new EvalException(EC.TLC_MODULE_ARGUMENT_ERROR,
				new String[] { "first", "TLCSet", "nonnegative integer", Values.ppr(vidx.toString()) });
	}
}
