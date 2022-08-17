package pcal;

import java.util.Vector;

import pcal.exception.PcalFixIDException;
import pcal.exception.PcalSymTabException;
import pcal.exception.PcalTLAGenException;
import pcal.exception.PcalTranslateException;
import pcal.exception.RemoveNameConflictsException;

/**
 * Responsible for generation of TLA+ from PCal AST<br>
 * Note: this class is renamed from NotYetImplemented on 11th March 2009
 * 
 * @author Leslie Lamport, Keith Marzullo
 * @version $Id$
 */
public class PCalTLAGenerator
{

    private PcalSymTab st = null;
    private AST ast;
             // This is set to the AST constructed by ParseAlgorithm.getAlgorithm

    private final ParseAlgorithm parseAlgorithm;

    /**
     * Constructs a working copy 
     * @param ast
     */
    public PCalTLAGenerator(final AST ast, final ParseAlgorithm parseAlgorithm)
    {
        this.ast = ast;
        this.parseAlgorithm = parseAlgorithm;
    }

    /********************************************************************
     * Called by trans.java.  Should go in a new .java file.            *
     ********************************************************************/
    public void removeNameConflicts() throws RemoveNameConflictsException
    {
        try
        {
            st = new PcalSymTab(ast);
        } catch (final PcalSymTabException e)
        {
            throw new RemoveNameConflictsException(e.getMessage());
        }

        st.Disambiguate();
        if (st.disambiguateReport.size() > 0)
            // SZ March 11, 2009. Warning reporting moved to PCalDebug 
            PcalDebug.reportWarning("symbols were renamed.");
        if (st.errorReport.length() > 0)
            throw new RemoveNameConflictsException(st.errorReport);
        try
        {
            PcalFixIDs.Fix(ast, st);
        } catch (final PcalFixIDException e)
        {
            throw new RemoveNameConflictsException(e.getMessage());
        }
    }

    /********************************************************************
     * The main translation method.  Should go in a new .java file.     *
     * Note that this requires RemoveNameConflicts to be called first   *
     * because of the grotty use of the class variable st.              *
     ********************************************************************/
    public Vector<String> translate() throws RemoveNameConflictsException
    {
        Vector<String> result = new Vector<>();
        AST xast;  // Set to the exploded AST

        PcalTranslate pcalTranslate = new PcalTranslate(this.parseAlgorithm);

        for (int i = 0; i < st.disambiguateReport.size(); i++)
            result.addElement(st.disambiguateReport.elementAt(i));
        try
        {
            xast = pcalTranslate.Explode(ast, st);
        } catch (final PcalTranslateException e)
        {
            throw new RemoveNameConflictsException(e);
        }
        // System.out.println("After exploding: " + xast.toString());
        try
        {
            final PcalTLAGen tlaGenerator = new PcalTLAGen(parseAlgorithm);
//            result.addAll(tlaGenerator.generate(xast, st));
            result = tlaGenerator.generate(xast, st, result);
        } catch (final PcalTLAGenException e)
        {
            throw new RemoveNameConflictsException(e);
        }

// tla-pcal debugging
/*******************************************************************
        * Following test added by LL on 31 Aug 2007.                       *
        *******************************************************************/
        try
        {
            if (parseAlgorithm.hasDefaultInitialization)
            {
                st.CheckForDefaultInitValue();
            }
        } catch (final PcalSymTabException e)
        {
            throw new RemoveNameConflictsException(e.getMessage());
        }
        return result;
    }
}
