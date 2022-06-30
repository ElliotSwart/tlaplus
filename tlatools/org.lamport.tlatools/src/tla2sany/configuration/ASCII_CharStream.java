// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
/* Generated By:JavaCC: Do not edit this line. ASCII_CharStream.java Version 0.7pre6 */
package tla2sany.configuration;

/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 */

public final class ASCII_CharStream
{
  public static final boolean staticFlag = true;
  static int bufsize;
  static int available;
  static int tokenBegin;
  static public int bufpos = -1;
  static private int bufline[];
  static private int bufcolumn[];

  static private int column = 0;
  static private int line = 1;

  static private boolean prevCharIsCR = false;
  static private boolean prevCharIsLF = false;

  static private java.io.Reader inputStream;

  static private char[] buffer;
  static private int maxNextCharInd = 0;
  static private int inBuf = 0;

  static private void ExpandBuff(final boolean wrapAround)
  {
     final char[] newbuffer = new char[bufsize + 2048];
     final int[] newbufline = new int[bufsize + 2048];
     final int[] newbufcolumn = new int[bufsize + 2048];

     try
     {
        if (wrapAround)
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           System.arraycopy(buffer, 0, newbuffer,
                                             bufsize - tokenBegin, bufpos);
           buffer = newbuffer;

           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
           System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
           bufline = newbufline;

           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
           System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
           bufcolumn = newbufcolumn;

           maxNextCharInd = (bufpos += (bufsize - tokenBegin));
        }
        else
        {
           System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
           buffer = newbuffer;

           System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
           bufline = newbufline;

           System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
           bufcolumn = newbufcolumn;

           maxNextCharInd = (bufpos -= tokenBegin);
        }
     }
     catch (final Throwable t)
     {
        throw new Error(t.getMessage());
     }


     bufsize += 2048;
     available = bufsize;
     tokenBegin = 0;
  }

  static private void FillBuff() throws java.io.IOException
  {
     if (maxNextCharInd == available)
     {
        if (available == bufsize)
        {
           if (tokenBegin > 2048)
           {
              bufpos = maxNextCharInd = 0;
              available = tokenBegin;
           }
           else if (tokenBegin < 0)
              bufpos = maxNextCharInd = 0;
           else
              ExpandBuff(false);
        }
        else if (available > tokenBegin)
           available = bufsize;
        else if ((tokenBegin - available) < 2048)
           ExpandBuff(true);
        else
           available = tokenBegin;
     }

     final int i;
     try {
        if ((i = inputStream.read(buffer, maxNextCharInd,
                                    available - maxNextCharInd)) == -1)
        {
           inputStream.close();
           throw new java.io.IOException();
        }
        else
           maxNextCharInd += i;
     }
     catch(final java.io.IOException e) {
        --bufpos;
        backup(0);
        if (tokenBegin == -1)
           tokenBegin = bufpos;
        throw e;
     }
  }

  static public char BeginToken() throws java.io.IOException
  {
     tokenBegin = -1;
     final char c = readChar();
     tokenBegin = bufpos;

     return c;
  }

  static private void UpdateLineColumn(final char c)
  {
     column++;

     if (prevCharIsLF)
     {
        prevCharIsLF = false;
        line += (column = 1);
     }
     else if (prevCharIsCR)
     {
        prevCharIsCR = false;
        if (c == '\n')
        {
           prevCharIsLF = true;
        }
        else
           line += (column = 1);
     }

     switch (c)
     {
        case '\r' :
           prevCharIsCR = true;
           break;
        case '\n' :
           prevCharIsLF = true;
           break;
        case '\t' :
           column--;
           column += (8 - (column & 07));
           break;
        default :
           break;
     }

     bufline[bufpos] = line;
     bufcolumn[bufpos] = column;
  }

  static public char readChar() throws java.io.IOException
  {
     if (inBuf > 0)
     {
        --inBuf;
        return (char)((char)0xff & buffer[(bufpos == bufsize - 1) ? (bufpos = 0) : ++bufpos]);
     }

     if (++bufpos >= maxNextCharInd)
        FillBuff();

     final char c = (char)((char)0xff & buffer[bufpos]);

     UpdateLineColumn(c);
     return (c);
  }

  

  

  static public int getEndColumn() {
     return bufcolumn[bufpos];
  }

  static public int getEndLine() {
     return bufline[bufpos];
  }

  static public int getBeginColumn() {
     return bufcolumn[tokenBegin];
  }

  static public int getBeginLine() {
     return bufline[tokenBegin];
  }

  static public void backup(final int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
       bufpos += bufsize;
  }

  public ASCII_CharStream(final java.io.Reader dstream, final int startline,
                          final int startcolumn, final int buffersize)
  {
    if (inputStream != null)
       throw new Error("""

               ERROR: Second call to the constructor of a static ASCII_CharStream.  You must
                   either use ReInit() or set the JavaCC option STATIC to false
                   during the generation of this class.""".indent(3));
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = buffersize;
    buffer = new char[buffersize];
    bufline = new int[buffersize];
    bufcolumn = new int[buffersize];
  }

  public ASCII_CharStream(final java.io.Reader dstream, final int startline,
                          final int startcolumn)
  {
     this(dstream, startline, startcolumn, 4096);
  }

  static public void ReInit(final java.io.Reader dstream, final int startline,
                            final int startcolumn, final int buffersize)
  {
    // Bug: The following line used to read "inputStream = dstream;" but this bug was
    // corrected by DRJ.  The correction should be in the .jcc copy, but isn't.
    inputStream = null;
    line = startline;
    column = startcolumn - 1;

    if (buffer == null || buffersize != buffer.length)
    {
      available = bufsize = buffersize;
      buffer = new char[buffersize];
      bufline = new int[buffersize];
      bufcolumn = new int[buffersize];
    }
    prevCharIsLF = prevCharIsCR = false;
    tokenBegin = inBuf = maxNextCharInd = 0;
    bufpos = -1;
  }

  static public void ReInit(final java.io.Reader dstream, final int startline,
                            final int startcolumn)
  {
     ReInit(dstream, startline, startcolumn, 4096);
  }

  public ASCII_CharStream(final java.io.InputStream dstream, final int startline,
                          final int startcolumn, final int buffersize)
  {
     this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  public ASCII_CharStream(final java.io.InputStream dstream, final int startline,
                          final int startcolumn)
  {
     this(dstream, startline, startcolumn, 4096);
  }

  static public void ReInit(final java.io.InputStream dstream, final int startline,
                            final int startcolumn, final int buffersize)
  {
     ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  static public void ReInit(final java.io.InputStream dstream, final int startline,
                            final int startcolumn)
  {
     ReInit(dstream, startline, startcolumn, 4096);
  }

  static public String GetImage()
  {
     if (bufpos >= tokenBegin)
        return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
     else
        return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                              new String(buffer, 0, bufpos + 1);
  }

  

  

  

}
