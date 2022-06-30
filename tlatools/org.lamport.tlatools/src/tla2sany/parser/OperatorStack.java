// Copyright (c) 2003 Compaq Corporation.  All rights reserved.
package tla2sany.parser;

import tla2sany.utilities.Vector;
import util.UniqueString;

/***************************************************************************
* An OperatorStack object is a stack of stacks of OSelement objects.       *
***************************************************************************/

public class OperatorStack implements tla2sany.st.SyntaxTreeConstants {

  private final Vector<Vector<OSelement>> StackOfStack = new Vector<>(10);
    /***********************************************************************
    * The actual OperatorStack object.  It appears to be a vector of       *
    * vectors of OSElement objects.  A stack appears to be represented by  *
    * a vector v where v.elementAt(v.size()-1) is the top element of the   *
    * stack.                                                               *
    ***********************************************************************/


  private Vector<OSelement> CurrentTop = null;
    /***********************************************************************
    * Appears to be the stack of OSelement objects at the top of the       *
    * OperatorStack.  It equals null if OperatorStack is empty.  Does it   *
    * equal null only if OperatorStack is empty?  J-Ch says yes.           *
    ***********************************************************************/
    
  private final ParseErrors PErrors;

  private final Operator fcnOp;
    /***********************************************************************
    * This is a constant set when the OperatorStack object is created to   *
    * the operator entry for "[", which apparently is a dummy operator     *
    * representing function application.                                   *
    ***********************************************************************/
    
  public OperatorStack(final ParseErrors pe ) {
    PErrors = pe;
    fcnOp = Operators.getOperator( UniqueString.uniqueStringOf("[") ); }

// could be optimized to reuse memory
  public final void newStack() {
    /***********************************************************************
    * Adds an empty stack to the top of the OperatorStack.                 *
    ***********************************************************************/
    CurrentTop = new Vector<>(20);
    StackOfStack.addElement( CurrentTop );
  }

// could be optimized to reuse memory
  public final void popStack() {
    /***********************************************************************
    * Removes the element at the top of the operator stack.                *
    ***********************************************************************/
    StackOfStack.removeElementAt( StackOfStack.size()-1 );
    if (StackOfStack.size() > 0 )
      CurrentTop = StackOfStack.elementAt( StackOfStack.size() - 1 );
    else
      CurrentTop = null;
  }

//
  public final boolean empty() {
    return CurrentTop.size()==0; // CurrentTop 
  }

  public final int size() {
    return CurrentTop.size();
  }

/*
  We use the prec and succ relations, as described in the formal specs of the grammar.
  Their definition is embedded in the Operator class.
  One way to look at prec and succ is in term of their effect on the stack:
  if left \prec right, then shift; if left \succ right, then reduce.
                            ^^^^^
What do left and right mean?????? What does shift mean????????
  There are caveats in the case of prefix or postfix operators.
*/

// returns true if the top of the stack holds a prefix or infix op
// used to distinguish [] from [] - or vice versa ?
// also used to distinguish junction from \/ or /\
  final public boolean preInEmptyTop() {
    /***********************************************************************
    * Returns true iff OperatorStack is empty, the stack at the top of     *
    * OperatorStack is empty, or OSElement at the top of the stack at the  *
    * top of OperatorStack is that of a prefix or infix operator.          *
    ***********************************************************************/
    if (CurrentTop == null) return true; // empty or holding nothing - lookahead compliant
    if (CurrentTop.size() == 0)
      return true;
    else {
      final Operator op  = CurrentTop.elementAt( CurrentTop.size()-1 ).getOperator();
      if (op != null)
        return op.isPrefix() || op.isInfix();
      else
        return false;
    }
  }

  final private void reduceInfix(final Operator op ) {
    /***********************************************************************
    * IF Len(CurrentTop) < 4                                               *
    *   THEN do nothing.                                                   *
    *   ELSE Suppose CurrentTop = <<head, op1, op2, op3>> \o rest          *
    *        and LET rightOp  == op1.node                                  *
    *                opNode   == op2.node                                  *
    *                leftNode == op3.node                                  *
    *        Set CurrentTop to <<head, OSelement(lSTN)>> \o rest, where    *
    *        lSTN = IF op represents an operator of type/kind N_Times      *
    *                 THEN IF leftOp is an operator of type/kind N_Times   *
    *                        THEN A node of type N_Times with heirs        *
    *                             leftOp.heirs \o <<opNode, rightNode>>    *
    *                        ELSE a node of type N_Times with heirs        *
    *                             <<leftOp, opNode, rightOp>>              *
    *                 ELSE a node of type N_InfixExpr with heirs           *
    *                      <<leftOp, opNode, rightOp>>                     *
    ***********************************************************************/
// System.err.println("infix reduction on " + op.toString() );
    final int n = CurrentTop.size()-1;
//    SyntaxTreeNode localTN = new InfixExprNode();
    if (n>=3) {
      final SyntaxTreeNode opNode  = CurrentTop.elementAt( n-2).getNode();
      final SyntaxTreeNode leftOp  = CurrentTop.elementAt( n-3).getNode();
      final SyntaxTreeNode rightOp = CurrentTop.elementAt( n-1).getNode();
      CurrentTop.removeElementAt(n-1);
      CurrentTop.removeElementAt(n-2);
      final SyntaxTreeNode lSTN;

      if (op.isNfix() && leftOp.isKind( N_Times ) ) {
        final SyntaxTreeNode[] children = (SyntaxTreeNode[])leftOp.heirs();
        final SyntaxTreeNode[] newC = new SyntaxTreeNode[ children.length + 2];
        System.arraycopy(children, 0, newC, 0, children.length);
        newC[ newC.length-2 ] = opNode;
        newC[ newC.length-1 ] = rightOp;
        lSTN = new SyntaxTreeNode( N_Times, newC );
      } else if (op.isNfix()) { // first \X encountered
        lSTN = new SyntaxTreeNode(N_Times, leftOp , opNode, rightOp );
      } else { // inFix
        lSTN = new SyntaxTreeNode(N_InfixExpr, leftOp , opNode, rightOp );
      }
      CurrentTop.setElementAt(new OSelement(lSTN), n-3);
    }
  }

  final private void reducePrefix() {
    /***********************************************************************
    * IF Len(CurrentTop) < 3                                               *
    *   THEN do nothing                                                    *
    *   ELSE suppose CurrentTop = <<head, next, third>> \o rest            *
    *        LET opNode == third.node                                      *
    *        Set CurrentTop to <<head, OSelement(lSTN)>> \o rest, where    *
    *        lSTN is a node of kind N_PrefixExpr with heirs                *
    *             third, next.                                             *
    ***********************************************************************/
      final int n = CurrentTop.size()-1;
//    SyntaxTreeNode localTN = new PrefixExprNode();
    if (n>=2) {
        final SyntaxTreeNode lSTN = new SyntaxTreeNode(N_PrefixExpr,
          CurrentTop.elementAt( n-2).getNode(),
          CurrentTop.elementAt( n-1).getNode());
        CurrentTop.removeElementAt(n-1);
      CurrentTop.setElementAt(new OSelement(lSTN) , n-2);
    }
  }

  final private void reducePostfix() {
    /***********************************************************************
    * IF Len(CurrentTop) < 3                                               *
    *   THEN do nothing                                                    *
    *   ELSE suppose CurrentTop = <<head, next, third>> \o rest            *
    *        LET op     == next.op                                         *
    *            opNode == next.node                                       *
    *        Set CurrentTop to <<head, OSelement(lSTN)>> \o rest, where    *
    *        lSTN = IF op = fcnOp                                          *
    *                 THEN a node of kind N_FcnAppl with heirs             *
    *                      third.node \o opNode.heirs                      *
    *                 ELSE a node of kind N_PostfixExpr with heirs         *
    *                      third.node, opNode.                             *
    ***********************************************************************/
      final int n = CurrentTop.size()-1;
    final SyntaxTreeNode lSTN;
//    SyntaxTreeNode localTN = new PostfixExprNode();
    if (n>=2) {
      final Operator op = CurrentTop.elementAt( n-1).getOperator();
      final SyntaxTreeNode opNode = CurrentTop.elementAt( n-1).getNode();
      if (op != fcnOp ) {
//      ((GenOpNode)opNode).register( op.getIdentifier(), STable);
        lSTN = new SyntaxTreeNode(N_PostfixExpr,
          CurrentTop.elementAt( n-2).getNode(),
          opNode);
      } else {
// System.out.println("postfix reduction : FcnOp");
        final SyntaxTreeNode eSTN = CurrentTop.elementAt( n-2).getNode();
        lSTN = new SyntaxTreeNode( eSTN.getFN(), N_FcnAppl, eSTN, (SyntaxTreeNode[]) (opNode.heirs()) );
      }
      CurrentTop.removeElementAt(n-1);
      CurrentTop.setElementAt(new OSelement(lSTN) , n-2);
    }
  }


// we follow case by case the definitions here.
// returns true if reduction succeeded, or was without effect.
  final public void reduceStack () throws ParseException {
    int n;
      /*********************************************************************
      * I presume java initializes n to 0                                  *
      *********************************************************************/
    Operator oR, oL;    // left and right operator
    OSelement tm0, tm1, tm2;

    do { // until (n != CurrentTop.size()-1);
      n = CurrentTop.size()-1; 
         // note !!! n is used as index, not size. XXX lousy identifier.
      tm0 = CurrentTop.elementAt( n );

      if ( ! tm0.isOperator() ) break; /* break = return here */
      oR = tm0.getOperator();
// System.out.println("tm0 est " + oR.toString() + printStack() );
      if ( oR.isPostfix() ) {
        if ( n == 0 ) {
          throw new ParseException("\n  Encountered postfix op " + 
                                   oR.getIdentifier() + " in block " + 
                                   tm0.getNode().getLocation().toString() + 
                                   " on empty stack");
        } else {
          tm1 = CurrentTop.elementAt( n-1 );
          if ( tm1.isOperator()) {
            oL = tm1.getOperator();
// System.out.println("tm1 est " + oL.toString() );
            if ( oL.isInfix() || oL.isPrefix() ) {
              throw new ParseException("\n  Encountered postfix op " + 
                                       oR.getIdentifier() + " in block " + 
                                       tm0.getNode().getLocation().toString() + 
                                       " following prefix or infix op " + 
                                       oL.getIdentifier() + ".");
            } else { // isPostfixOp - must reduce.
              reducePostfix();
            }
          } else { // tm1 is Expression - what's below ?
            if ( n > 1 ) {
              tm2 = CurrentTop.elementAt( n-2 );
              if ( tm2.isOperator() ) { 
                oL = tm2.getOperator();
                if ( Operator.succ( oL, oR ) ) { // prefix or infix ?
                   if ( oL.isInfix() ) reduceInfix( oL ); else reducePrefix();
                } else if ( ! Operator.prec( oL, oR ) ) {
                  throw new ParseException(
                           "Precedence conflict between ops " + 
                           oL.getIdentifier() + " in block " + 
                           tm0.getNode().getLocation().toString() + " and " 
                           + oR.getIdentifier() + ".");
		} else
		  break;
              } else {
                throw new ParseException(
                  "Expression at location " + 
                  tm2.getNode().getLocation().toString() +
                  " and expression at location " + 
                  tm1.getNode().getLocation().toString() +
                  " follow each other without any intervening operator.");
              }
            } else
              break; // nothing this time around.
          }
        }
      } else if ( oR.isPrefix() ) {
        if ( n == 0 ) break; // can't do anything yet
        else {
          tm1 = CurrentTop.elementAt( n-1 );
          if ( tm1.isOperator()) { // prefix, or infix
            oL = tm1.getOperator();
            if ( oL.isPostfix() ) {
              throw new ParseException(
                      "\n  Encountered prefix op " + oR.getIdentifier() + 
                      " in block " + tm0.getNode().getLocation().toString() + 
                      " following postfix op " + oL.getIdentifier() + ".");
            } else // nothing to do
              break;
          } else { // can't be expression 
            throw new ParseException(
                          "\n  Encountered prefix op " + oR.getIdentifier() + 
                          " in block " + 
                          tm0.getNode().getLocation().toString() + 
                          " following an expression.");
          }
        }
      } else { // oR.isInfix()
        if ( n == 0 ) {
            final Operator mixR  = Operators.getMixfix( oR );
            if ( mixR == null )
              throw new ParseException(
                          "\n  Encountered infix op " + oR.getIdentifier() + 
                          " in block " + 
                          tm0.getNode().getLocation().toString() + 
                          " on empty stack.");
            else
              break;
        } else {
// System.out.println("infix case: " + oR.getIdentifier());
          tm1 = CurrentTop.elementAt( n-1 );
          if ( tm1.isOperator()) {
            oL = tm1.getOperator();
            if ( oL.isInfix() || oL.isPrefix() ) { 
                    // new case for mixfix XXX this is not exhaustive.
              final Operator mixR  = Operators.getMixfix( oR );
              if ( mixR == null ) { // is infix
                if (oR == Operator.VoidOperator() )
                  throw new ParseException(
                         "\n  Missing expression in block " + 
                         tm1.getNode().getLocation().toString() + 
                         " following prefix or infix op " + 
                         oL.getIdentifier() + ".");
                else
                  throw new ParseException(
                              "\n  Encountered infix op " + 
                              oR.getIdentifier() + " in block " + 
                              tm1.getNode().getLocation().toString() + 
                              " following prefix or infix op " + 
                              oL.getIdentifier() + ".");
              } else if (   Operator.succ( oL, mixR ) 
                         || ( oL == mixR && oL.assocLeft() ) ) {
// System.out.println("precedence pbm detected");
                throw new ParseException(
                            "\n  Precedence conflict between ops " + 
                            oL.getIdentifier() + " in block " + 
                            tm1.getNode().getLocation().toString() + 
                            " and " + mixR.getIdentifier() + ".");
              } // else, skip
            } else // no choice
              reducePostfix();
          } else { // tm1 is Expression - what's below ?
// System.out.println("tm1 is expression");
            if ( n > 1 ) {
// System.out.println("deep stack");
              tm2 = CurrentTop.elementAt( n-2 );
              if ( tm2.isOperator() ) {
                oL = tm2.getOperator();
// System.out.println("tm2 is operator: " + oL.getIdentifier());
                final Operator mixL = Operators.getMixfix( oL );
                if (  mixL != null && ((n==2) 
                    || 
                     CurrentTop.elementAt( n-3 ).isOperator())) {
// System.out.println("identified prefix");
                  oL = mixL;
                } 
                if (  Operator.succ( oL, oR ) 
                    ||
                     ( oL == oR && oL.assocLeft() ) ) { 
                         // prefix or infix ?
// System.out.println("prefix or infix in succ oL, oR");
                   if      ( oL.isInfix() ) reduceInfix(oL);
                   else if ( oL.isPrefix() ) reducePrefix();
                   else {
                     if (   (tm2.getNode().getLocation().beginLine() < 
                                tm1.getNode().getLocation().beginLine() )
                         && (oR.getIdentifier() == 
                               UniqueString.uniqueStringOf("=") )) {
// System.out.println("Case 1");
                       throw new ParseException(
                              "\n  *** Hint *** You may have mistyped ==" + 
                              "\n  Illegal combination of operators " + 
                              oL.getIdentifier() + " in block " + 
                              tm2.getNode().getLocation().toString() + 
                              " and " + oR.getIdentifier() + ".");
		     } else if (oR == Operator.VoidOperator() ) {
// System.out.println("Case 2");
	               throw new ParseException(
                               "\n Error following expression at  " + 
                               tm2.getNode().getLocation().toString() + 
                               ", missing operator or separator.");
		     } else {
// System.out.println("Case 3");
                       throw new ParseException(
                              "\n  Illegal combination of operators " + 
                              oL.getIdentifier() + " in block " + 
                              tm2.getNode().getLocation().toString() + 
                              " and " + oR.getIdentifier() + ".");
		     }
                  }
                } else if (   !( Operator.prec( oL, oR ) 
                           || (oL==oR && oL.assocRight())) ) {
// System.out.println("prefix or infix in prec oL, oR+++ throwing exception");
                    throw new ParseException(
                            "\n  Precedence conflict between ops " + 
                            oL.getIdentifier() + " in block " + 
                            tm2.getNode().getLocation().toString() + 
                            " and " + oR.getIdentifier() + ".");
                } else {
// System.out.println("Case not handled");
                  break;
		}
              } else {
                throw new ParseException(
                        "Expression at location " + 
                        tm2.getNode().getLocation().toString() +
                        " and expression at location " + 
                        tm1.getNode().getLocation().toString() +
                        " follow each other without any " + 
                        "intervening operator.");
              }
            } else {
// System.out.println("Other case  not handled");
              break; // nothing this time around.
	    }
          }
        }
      }
    } while (n != CurrentTop.size()-1);
// System.out.println("exit at size: " + (CurrentTop.size()-1));
  }

  final public SyntaxTreeNode finalReduce() throws ParseException {

    int n=0;
    pushOnStack( null, Operator.VoidOperator() );
    reduceStack();
    if ( isWellReduced() )
      return CurrentTop.elementAt(0).getNode();
    else {
      final StringBuilder msg = new StringBuilder("Couldn't properly parse expression");
      do {
//((OSelement)CurrentTop.elementAt(n)).getNode().printTree(new java.io.PrintWriter(System.out));  n++;
        msg.append("-- incomplete expression at ");
        msg.append( CurrentTop.elementAt(n).getNode().getLocation().toString() ) ;
        msg.append(".\n");
        n++;
      } while (n < CurrentTop.size()-1);
      PErrors.push( new ParseError( msg.toString(), "-- --" ));
      return null;
    }
  }

  public boolean isWellReduced () {
     return CurrentTop.size() == 2;
  }

  final public void pushOnStack(final SyntaxTreeNode n, final Operator o ) {
    /* XXX could be optimized to reuse OSelements */
    /***********************************************************************
    * Apparently, the operator argument o is null if this is not an        *
    * in/pre/postfix operator.                                             *
    ***********************************************************************/
      CurrentTop.addElement( new OSelement( n, o) );
//    System.out.println(printStack());
  }

  public SyntaxTreeNode bottomOfStack() {
    return CurrentTop.elementAt(0).getNode();
  }

  public final void reduceRecord(final SyntaxTreeNode middle, final SyntaxTreeNode right ) throws ParseException {
    int index;
    OSelement oselt;

    index = CurrentTop.size()-1;
    if (index < 0)
      throw new ParseException("\n    ``.'' has no left hand side at " + middle.getLocation().toString() + "." );
    oselt = CurrentTop.elementAt( index );

    if ( oselt.isOperator() ) {
      final OSelement ospelt = CurrentTop.elementAt( index - 1 );
      if ( oselt.getOperator().isPostfix() && !ospelt.isOperator() ) {
        CurrentTop.addElement( null ); // humour reducePostfix
        reducePostfix();
        index = CurrentTop.size()-1;             // fix humoring
        CurrentTop.removeElementAt(index);
        index--;
        oselt = CurrentTop.elementAt( index );
      } else
        throw new ParseException("\n    ``.'' follows operator " + oselt.getNode().getLocation().toString() + "." );
    } 
    final SyntaxTreeNode left = CurrentTop.elementAt(index ).getNode();
    final SyntaxTreeNode rcd = new SyntaxTreeNode(N_RecordComponent, left, middle, right);
    CurrentTop.setElementAt(new OSelement(rcd) , index);
}

// simple utility

  final public OSelement topOfStack() {
    /***********************************************************************
    * Returns the top element of the top stack, or null if that stack is   *
    * empty.                                                               *
    ***********************************************************************/
    if (CurrentTop == null) {return null;}
      final int n = CurrentTop.size() ;
    if (n == 0) {return null;}
      return CurrentTop.elementAt(n-1) ;
   }

  final public void popCurrentTop() {
    /***********************************************************************
    * Removes the element currently at the top of the CurrentTop stack.    *
    * Must not be called unless CurrentTop has at least one element.       *
    ***********************************************************************/
    CurrentTop.removeElementAt(CurrentTop.size() - 1) ;
   }
}
