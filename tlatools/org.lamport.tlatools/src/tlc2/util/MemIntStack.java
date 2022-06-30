// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
// Portions Copyright (c) 2003 Microsoft Corporation.  All rights reserved.
// Last modified on Mon 30 Apr 2007 at 13:26:33 PST by lamport
//      modified on Thu Jan 10 18:33:42 PST 2002 by yuanyu

package tlc2.util;

public final class MemIntStack extends MemBasedSet implements IntStack {
	private static final int MIN_CAPACITY = 1024;

	public MemIntStack(final String diskdir, final String name) {
		super(MIN_CAPACITY);
	}

	/* (non-Javadoc)
	 * @see tlc2.util.IntStack#pushInt(int)
	 */
	public final synchronized void pushInt(final int x) {
		if (this.size == this.elems.length) {
			final int[] newElems = ensureCapacity(MIN_CAPACITY);
			System.arraycopy(elems, 0, newElems, 0, this.size);
			this.elems = newElems;
		}
		this.elems[this.size] = x;
		this.size++;
	}

	/* (non-Javadoc)
	 * @see tlc2.util.IntStack#pushLong(long)
	 */
	public final synchronized void pushLong(final long x) {
		this.pushInt((int) (x & 0xFFFFFFFFL));
		this.pushInt((int) (x >>> 32));
	}

	/* (non-Javadoc)
	 * @see tlc2.util.IntStack#popInt()
	 */
	public final synchronized int popInt() {
		return this.elems[--this.size];
	}

	public final synchronized int peakInt() {
		return peakInt(size - 1);
	}

	public final synchronized int peakInt(final int pos) {
		return this.elems[pos];
	}

	/* (non-Javadoc)
	 * @see tlc2.util.IntStack#popLong()
	 */
	public final synchronized long popLong() {
		final long high = this.popInt();
		final long low = this.popInt();
		return (high << 32) | (low & 0xFFFFFFFFL);
	}

	public final synchronized long peakLong() {
		final long high = this.peakInt();
		final long low = this.peakInt();
		return (high << 32) | (low & 0xFFFFFFFFL);
	}

	public final synchronized long peakLong(final int pos) {
		final long high = this.peakInt(pos + 1);
		final long low = this.peakInt(pos);
		return (high << 32) | (low & 0xFFFFFFFFL);
	}

	/* (non-Javadoc)
	 * @see tlc2.util.IntStack#reset()
	 */
	public final void reset() {
		this.size = 0;
	}
}
