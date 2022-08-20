// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 15:29:57 PST by lamport
//      modified on Fri Mar  2 15:37:34 PST 2001 by yuanyu

package tlc2.tool;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * This class represents a TLA+ state vector.
 * This is the mutable version, which means that in-place
 * updates are used for improved performance and reduced
 * allocation.
 */
public final class StateVec extends ArrayList<TLCState> implements IStateFunctor, INextStateFunctor {

    public StateVec(int capacity){
      super(capacity);
    }

    public StateVec(TLCState[] states){
      super(states.length);

        this.addAll(Arrays.asList(states));
    }

    public boolean isLastElement(final TLCState state) {
        if (isEmpty()) {
            return false;
        }
        return this.get(size() - 1) == state;
    }

    public TLCState first() {
        return get(0);
    }

    public StateVec addElement(final TLCState state) {
        this.add(state);
        return this;
    }

    public StateVec addElement(final TLCState predecessor, final Action action, final TLCState state) {
        this.add(state.setPredecessor(predecessor).setAction(action));
        return this;
    }

    public void deepNormalize() {
        for (var state : this) {
            state.deepNormalize();
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (this.size() > 0) {
            sb.append(this.get(0).toString());
        }
        for (int i = 1; i < this.size(); i++) {
            sb.append(", ");
            sb.append(this.get(i).toString());
        }
        sb.append("}");
        return sb.toString();
    }

    public boolean contains(final TLCState state) {
        return this.stream().anyMatch(s -> s.fingerPrint() == state.fingerPrint());
    }

    @Override
    public boolean hasStates() {
        return !isEmpty();
    }
}
