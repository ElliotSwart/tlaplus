// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.utilities;

/**
 * Implementation of assertion for SANY. 
 * 
 * @author Simon Zambrovski
 * @version $Id$
 * 
 * @deprecated Use {@linkplain util.Assert} instead of this class
 */
public class Assert
{

    // SZ Jul 13, 2009: fishy way to exit the program
    /**
     * @deprecated Use {@linkplain util.Assert#check(boolean, int, String[])} instead of this method
     */
    public final static void assertion(final boolean b)
    {
    }

    // SZ Jul 13, 2009: method not used
    /**
     * @deprecated Use {@linkplain util.Assert#fail(int, String[])} instead of this method
     */
    public final static void fail(final String msg)
    {
    }

}
