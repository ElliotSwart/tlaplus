// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.utilities;

public class
Stack extends tla2sany.utilities.Vector<Object> {

  public Stack() { super(); }

  

  public final boolean empty() {
    return size==0;
  }

  public final Object peek() {
    if (size == 0 )
      throw new java.util.EmptyStackException();
    else
      return info[ size -1 ];
  }

  public final Object pop() {
    if (size == 0 )
      throw new java.util.EmptyStackException();
    else
      return info[ size-- -1 ];
  }

  public final Object push(final Object obj) {
    super.addElement( obj );
    return obj;
  }

  public final int search(final Object obj ){
   for (int lvi = size-1; lvi >= 0; lvi-- )
     if ( obj == info[ lvi ] ) return lvi;
   return -1;
  }
}
