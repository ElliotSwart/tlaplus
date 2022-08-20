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
<<<<<<< HEAD
  }

  public StateVec(final StateVec other) {
	this(other.size);
	this.size = other.size;
	for (int i = 0; i < v.length; i++) {
		this.v[i] = other.get(i);
	}
  }
  
  public StateVec(final TLCState[] v) {
    this.v = v;
    this.size = v.length;
  }

  public boolean empty() { return (this.size == 0); }

  public int size() { return this.size; }
=======
>>>>>>> potentially-working-standardize

    public StateVec(TLCState[] states){
      super(states.length);

        this.addAll(Arrays.asList(states));
    }

<<<<<<< HEAD
  public TLCState get(final int i) { return this.v[i]; }

  public boolean isLastElement(final TLCState state) {
	  if (isEmpty()) {
		  return false;
	  }
	  return this.get(size() - 1) == state;
  }
  
  public TLCState first() {
	return get(0);
  }

  public void clear() {
    this.size = 0;
  }
  
  /* (non-Javadoc)
   * @see tlc2.tool.IStateFunction#add(tlc2.tool.TLCState)
   */
  @Override
  public StateVec add(final TLCState state) {
    if (this.size >= this.v.length) { grow(1); }
    this.v[this.size++] = state;
    return this;
  }

  @Override
  public StateVec add(final TLCState predecessor, final Action action, final TLCState state) {
	  return add(state.setPredecessor(predecessor).setAction(action));
  }
 
  public StateVec addElements(StateVec s1) {
    StateVec s0 = this;

    if (s1.size > s0.size) {
      final StateVec tmp = s0;
      s0 = s1;
      s1 = tmp;
=======
    public boolean isLastElement(final TLCState state) {
        if (isEmpty()) {
            return false;
        }
        return this.get(size() - 1) == state;
>>>>>>> potentially-working-standardize
    }

    public TLCState first() {
        return get(0);
    }

<<<<<<< HEAD
  public void remove(final int index) {
    this.v[index] = this.v[this.size-1];
    this.size--;
  }

  public StateVec copy() {
    final TLCState[] res = new TLCState[this.size];
    for (int i = 0; i < this.size; i++) {
      res[i] = this.v[i].copy();
=======
    public StateVec addElement(final TLCState state) {
        this.add(state);
        return this;
>>>>>>> potentially-working-standardize
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
