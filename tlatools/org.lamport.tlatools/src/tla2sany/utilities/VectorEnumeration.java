// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.utilities;
import java.util.Enumeration;

final class VectorEnumeration<E> implements Enumeration<E> {
  int index = 0;
  final Object[] data;

  VectorEnumeration(final Object[] info, final int size ) {
    data = new Object[ size ];
    System.arraycopy( info, 0, data, 0, size );
  }

  @Override
  public boolean hasMoreElements() {
    return index < data.length;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E nextElement() {
    if (index < data.length)
      return (E)data[index++];
    else
      throw new java.util.NoSuchElementException();
  }
}
