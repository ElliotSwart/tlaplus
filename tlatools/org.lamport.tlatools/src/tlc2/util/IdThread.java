// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
package tlc2.util;

import tlc2.tool.AbstractChecker;
import tlc2.tool.Simulator;
import tlc2.tool.TLCState;
import tlc2.tool.impl.Tool;
import tlc2.value.IValue;

import java.util.Objects;

/**
 * An <code>IdThread</code> is a <code>Thread</code> with an
 * integer identifier.
 */

public class IdThread extends Thread {
    private static final ThreadLocal<TLCState> currentState = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> usingModelChecker = new ThreadLocal<>();

    /* Used only for TLASet operator */
    private static final ThreadLocal<Simulator> simulator = new ThreadLocal<>();

    private static final ThreadLocal<AbstractChecker> mainChecker = new ThreadLocal<>();
    private final int id;
    private IValue[] localValues = new IValue[4];

    /**
     * Create a new thread with ID <code>id</code>.
     */
    public IdThread(final int id) {
        this.id = id;
    }

    public IdThread(final Runnable runnable, final String name, final int id) {
        super(runnable, name);
        this.id = id;
    }

    /**
     * @return null during the generation of initial states (see
     * tlc2.tool.ModelChecker.doInit(boolean) and a TLCState during the
     * generation of next-states in tlc2.tool.ModelChecker.doNext(TLCState,
     * ObjLongTable, Worker). It corresponds to the predecessor state of the
     * next-state currently being generated by the worker thread.
     */
    public static TLCState getCurrentState() {
        return currentState.get();
    }

    public static void setCurrentState(final TLCState state) {
        currentState.set(state);
    }

    public static Simulator getSimulator() {
        return IdThread.simulator.get();
    }

    public static void setSimulator(final Simulator simulator) {
        IdThread.simulator.set(simulator);
    }

    public static AbstractChecker getMainChecker() {
        return IdThread.mainChecker.get();
    }

    public static void setMainChecker(final AbstractChecker mainChecker) {
        IdThread.mainChecker.set(mainChecker);
    }

    public static void resetThreadState() {
        IdThread.mainChecker.remove();
        IdThread.simulator.remove();
        IdThread.usingModelChecker.remove();
        resetCurrentState();
    }

    public static TLCState resetCurrentState() {
        final TLCState tlcState = currentState.get();
        currentState.remove();
        return tlcState;
    }

    public static Boolean getUsingModelChecker() {
        var u = IdThread.usingModelChecker.get();

        return Objects.requireNonNullElse(u, false);
    }

    public static void setUsingModelChecker(Tool.Mode usingModelChecker) {
        IdThread.usingModelChecker.set(usingModelChecker == Tool.Mode.MC || usingModelChecker == Tool.Mode.MC_DEBUG);
    }

    /**
     * Return the Id of the calling thread. This method
     * will result in a <TT>ClassCastException</TT> if
     * the calling thread is not of type <TT>IdThread</TT>.
     */
    public static int GetId() {
        return ((IdThread) Thread.currentThread()).id;
    }

    /**
     * If the calling thread is of type <TT>IdThread</TT>,
     * return its ID. Otherwise, return <TT>otherId</TT>.
     */
    public static int GetId(final int otherId) {
        final Thread th = Thread.currentThread();
        return (th instanceof IdThread idT) ? idT.id : otherId;
    }

    /**
     * Return this thread's ID.
     */
    // This method was originally called getId, but a later
    // version of Java introduced a getId method into the Thread
    // class which returned a long, and it doesn't allow it to be
    // overridden by a method that returns an int.  So
    // LL changed the return type from int to long on 31 Aug 2007
    // However, apparently a later version of Java complained when
    // its use in class tlc.tool.liveness.LiveWorker assigned
    // its value to an int variable, so it was renamed myGetId.
    // It is used only in LiveWorker.
    public final int myGetId() {
        return this.id;
    }

    public IValue getLocalValue(final int idx) {
        if (idx < this.localValues.length) {
            return this.localValues[idx];
        }
        return null;
    }

    public IValue[] getLocalValues() {
        return this.localValues;
    }

    public void setLocalValue(final int idx, final IValue val) {
        if (idx >= this.localValues.length) {
            final IValue[] vals = new IValue[idx + 1];
            System.arraycopy(this.localValues, 0, vals, 0, this.localValues.length);
            this.localValues = vals;
        }
        this.localValues[idx] = val;
    }
}
