/***************************************************************************
* CLASS PcalSymTab                                                         *
* last modified on Fri 31 Aug 2007 at 15:44:54 PST by lamport               *
*      modified on Tue 30 Aug 2005 at 18:30:10 UT by keith                 *
*                                                                          *
* This class implements the symbol table corresponding with an AST object. *
*                                                                          *
* symtab is the generated symbol table and is a vector of SymTableEntry.   *
* procs is the generated vector of ProcedureEntry (which includes the      *
*   entry point label),  and processes is the generated vector of          *
*   ProcessEntry.                                                          *
* If the algorithm is a uniprocess, then iPC is the initial label.         *
*                                                                          *
* The string disambiguateReport are TLA comments that describe how         *
* variables and labels were renamed.                                       *
*                                                                          *
* The string errorreport are errors that were generated while creating     *
* the symbol table.                                                        *
*                                                                          *
* The public methods of this class are the following.  Except as noted,    *
* they are not called from outside this file.                              *
*                                                                          *
*     boolean InsertSym (char type, String id, String context, int line,   *
*                             int col)                                     *
*          Inserts the values into the symbol table. Returns true if the   *
*          value was not in the symbol table before the insert.            *
*                                                                          *
*     int FindSym (char type, String id, String context)                   *
*          Returns the index of this symbol in the symbol table.           *
*                                                                          *
*     boolean InsertProc (AST.Procedure ast)                               *
*          Inserts ast.params and ast.decls into the procedure table.      *
*          Returns true if value was not in  table before the insert.      *
*                                                                          *
*     boolean InsertProcess (AST.Process p)                                *
*          Inserts an entry into the process table vector. Returns true    *
*          is the value was not in the  table before the insert.           *
*                                                                          *
*     int FindSym (char type, String id, String context)                   *
*          Returns the index of this symbol in the symbol table.           *
*     (there are other versions of this method too).                       *
*                                                                          *
*     String UseThis (char type, String id, String context)                * 
*          Returns the disambiguated id of this symbol.                    *
*     (there are other versions of this method too).                       *
*          Called from PcalFixIDs and PcalTranslate.                       *
*                                                                          *
*     void Disambiguate ( )                                                *
*          Generates the disambiguated names in the symbol table.          *
*          Called from NotYetImplemented                                   *
*                                                                          *
*     String toString ( )                                                  *
*          The symbol table represented as a string; useful for debugging  *
*          Impossible to figure out which of the zillions of invocations   *
*          of toString are calling this method.                            *
*                                                                          *
*     void CheckForDefaultInitValue ( )                                    *
*          Reports an error if "defaultInitValue" appears in the symbol    *
*          table.  Added by LL on 31 Aug 2007.                             *
*                                                                          *
* This method does not save the complete AST of the algorithm, which gives *
* some clue to what the methods do.                                        *
*                                                                          *
* TO REVISE: The mapping of id to symbol, via UseThis, is sloppy.          *
****************************************************************************/

package pcal;

import java.util.Vector;

import pcal.exception.PcalSymTabException;

public class PcalSymTab {
    public final Vector<SymTabEntry> symtab;             // Vector of SymTabEntry
    public final Vector<ProcedureEntry> procs;              // Vector of ProcedureEntry
    public final Vector<ProcessEntry> processes;          // Vector of ProcessEntry
    public final Vector<String> disambiguateReport; // Vector of String (comments)
    public String errorReport;        // Accumulated errors
    public String iPC;                // initial pc value for unip algorithm

    // Symbol types. The order  determines priority in terms of constructing a
    // disambiguous name.
    public static final int num_vtypes = 7;
    public static final int GLOBAL = 0;   
    public static final int LABEL = 1;
    public static final int PROCEDURE = 2;
    public static final int PROCESS = 3;
    public static final int PROCESSVAR = 4;
    public static final int PROCEDUREVAR = 5;
    public static final int PARAMETER = 6;

    // The following two arrays need to be ordered wrt the constants above.

    // Prepend this type-specific string to name before disambiguation.
    private static final String[] typePrefix = { "", "", "", "", "", "", "" };

    // For toString method.
    public static final String[] vtypeName = {
        "Global variable", "Label", "Procedure", "Process", 
        "Process variable", "Procedure variable", "Parameter"};

    /* NESTED CLASS: Symbol table entries */
    public static class SymTabEntry {
        public final int type;       // variable type
                               // can be GLOBAL, LABEL, PROCEDURE, PROCESS, PROCESSVAR,
                               // PROCEDUREVAR, or PARAMETER, declared above.
        public final String id;      // original name
        public final String context; // where defined
                               // experimentation shows that context is:
                               //    "" if cType = ""
                               //    the name of a process if cType = "process"
                               //    the name of a procedure if cType = "procedure"
        public final String cType;   // procedure, process or empty
        public final int line;       // line where defined
        public final int col;        // column where defined
        public String useThis; // Disambiguated name

        public SymTabEntry(final int type,
                           final String id,
                           final String context,
                           final String cType,
                           final int line,
                           final int col) {
            this.type = type;
            this.id = id;
            this.context = context;
            this.cType = cType;
            this.line = line;
            this.col = col;
            this.useThis = id;
        }
        
        /**
         * For debugging:
         */
        public String toString() {
        	return "[id: " + this.id + ", usethis: " + this.useThis + 
        			", line: " + this.line + ", col:" + this.col +
        			", type: " + this.type + ", context: " + this.context + "]";
        }
    } /* End of SymTabEntry */

    /* NESTED CLASS: Procedure table entries */
    public static class ProcedureEntry {
        public String name;    // Procedure name
        public final Vector<AST.PVarDecl> params;  // of PVarDecl
        public final Vector<AST.PVarDecl> decls;   // of PVarDecl
        public String iPC;     // initial label of procedure
        public final AST.Procedure ast; // AST of the procedure
                                  // Added 13 Jan 2011 by LL 
        
        public ProcedureEntry(final AST.Procedure p) {
            this.name = p.name;
            this.params = p.params;
            this.decls = p.decls;
            this.ast = p;
            if (p.body.size() == 0) this.iPC = null;
            else {
                final AST.LabeledStmt ls = (AST.LabeledStmt) p.body.elementAt(0);
                this.iPC = ls.label;
            }
        }
    } /* End of ProcedureEntry */

    /* NESTED CLASS: Process table entries */
    public static class ProcessEntry {
        public String name;      // Process name
        public final boolean isEq;     // true means "=", false means "\\in"
        public final TLAExpr id;       // set of identifiers or identifier
        public final Vector<?> decls;     // of ParDecl
        public String iPC;       // Initial pc of this process
        public final AST.Process ast; // AST of the procedure
        // Added 13 Jan 2011 by LL 

        public ProcessEntry(final AST.Process p) {
            this.name = p.name;
            this.isEq = p.isEq;
            this.id = p.id;
            this.decls = p.decls;
            this.ast = p;
            if (p.body.size() == 0) this.iPC = null;
            else {
                final AST.LabeledStmt ls = (AST.LabeledStmt) p.body.elementAt(0);
                this.iPC = ls.label;
            }
        }
    } /* End of ProcessEntry */

    /**
     * As should be perfectly obvious from the name, this method constructs
     * the symbol table for the AST ast, which I presume contains all things
     * whose name must be looked up, which includes labels, variables, and
     * probably process and procedure names.
     * 
     * @param ast
     * @throws PcalSymTabException
     */
    public PcalSymTab (final AST ast) throws PcalSymTabException {

        symtab = new Vector<>();
        iPC = null;
        disambiguateReport = new Vector<>();
        procs = new Vector<>();
        processes = new Vector<>();
        errorReport = "";
// Following line removed by LL on 3 Feb 2006
//        InsertSym(LABEL, "Done", "", "", 0, 0);

        InsertSym(GLOBAL, "pc", "", "", 0, 0);
        ExtractSym(ast, "");
        if (errorReport.length() > 0) throw new PcalSymTabException(errorReport);
    }

    /***************************************************
     * TRUE if inserted; false if was already in table *
     * Can not insert a variable of name x if there is *
     * a global with name x or another variable in the *
     * same context with name x.                       *
    /***************************************************/
    public boolean InsertSym (final int type,
                              final String id,
                              final String context,
                              final String cType,
                              final int line, final int col) {
        int i;
        if (type == PROCEDUREVAR || type == PROCESSVAR || type == PARAMETER) {
            i = FindSym(GLOBAL, id, "");
            if (i < symtab.size()) return false; /* GLOBAL with same id exists */
            i = FindSym(id, context);
            if (i < symtab.size()) return false; /* id in same context exists */
        }
        else {
            i = FindSym(type, id, context);
            if (i < symtab.size()) return false;
        }
        final SymTabEntry se = new SymTabEntry(type, id, context, cType, line, col);
        symtab.addElement(se);
        return true;
    }

    /***************************************************
     * Insert procedure table entry.                   *
     * TRUE if inserted; false if was already in table *
     ***************************************************/
    public boolean InsertProc (final AST.Procedure proc) {
        final int i = FindProc(proc.name);
        if (i < procs.size()) return false;
        final ProcedureEntry pe = new ProcedureEntry(proc);
        procs.addElement(pe);
        return true;
    }

    /***************************************************
     * Insert process table entry.                     *
     * TRUE if inserted; false if was already in table *
     ***************************************************/
    public boolean InsertProcess(final AST.Process p) {
        final int i = FindProcess(p.name);
        if (i < processes.size()) return false;
        final ProcessEntry pe = new ProcessEntry(p);
        processes.addElement(pe);
        return true;
    }

    /*********************************************************
     * Various ways to look up something in the symbol table.*
     * Returns the index in the table. If the index equals   *
     * symtab.size() (which makes no sense), then it isn't   *
     * in the symbol table.                                  *
     *********************************************************/
    public int FindSym (final int type, final String id, final String context) {
        int i = 0;
        while (i < symtab.size()) {
            final SymTabEntry se = symtab.elementAt(i);
            if (se.id.equals(id) && se.context.equals(context)
                && se.type == type) return i;
            i = i + 1;
        }
        return i;
   }

    /*********************************************************
     * Returns first it finds with id and context, no matter *
     * what type it is.                                      *
     *********************************************************/
    public int FindSym (final String id, final String context) {
        int i = 0;
        while (i < symtab.size()) {
            final SymTabEntry se = symtab.elementAt(i);
            if (se.id.equals(id) && se.context.equals(context))
                return i;
            i = i + 1;
        }
        return i;
   }

    /*********************************************************
     * Returns index of entry in procedure table. If it isn't*
     * in the table, then returns procs.size().              *
     *********************************************************/
    public int FindProc (final String id) {
        int i = 0;
        while (i < procs.size()) {
            final ProcedureEntry pe = procs.elementAt(i);
            if (pe.name.equals(id)) return i;
            i = i + 1;
        }
        return i;
   }

    /*********************************************************
     * Returns index of entry in process table. If it isn't  *
     * in the table, then returns procs.size().              *
     *********************************************************/
    public int FindProcess (final String id) {
        int i = 0;
        while (i < processes.size()) {
            final ProcessEntry pe = processes.elementAt(i);
            if (pe.name.equals(id)) return i;
            i = i + 1;
        }
        return i;
   }

    /*********************************************************
     * Routines for returning disambiguated names.           *
     *********************************************************/
    
    /* Return the disambiguated name for a type X id X context */
    public String UseThis (final int type, final String id, final String context) {
        final int i = FindSym(type, id, context);
        if (i == symtab.size()) return id;
        else return symtab.elementAt(i).useThis;
    }

    

    /*********************************************************
     * Given a variable referenced in a context. First get   *
     * the entry in the context. If no such variable, then   *
     * see if a global one exists. If not, then use it       *
     * undisambiguated.                                      *
     * NOTE: Stop using FindSym; it's a bad hack.            *
     *********************************************************/
    public String UseThisVar (final String id, final String context) {
        SymTabEntry se = null;
        int i = FindSym(id, context);
        if (i == symtab.size()) return id;
        se = symtab.elementAt(i);
        if (se.type == GLOBAL || se.type == PROCESSVAR
            || se.type == PROCEDUREVAR || se.type == PARAMETER)
            return se.useThis;
        i = FindSym(id, "");
        if (se.type == GLOBAL) return se.useThis;
        return id;
    }

    

    /*********************************************************
     * TRUE if id is ambiguous.                              *
     *********************************************************/
    private boolean Ambiguous (final String id) {
        int i = 0;
        boolean found = false;
        while (i < symtab.size()) {
            final SymTabEntry se = symtab.elementAt(i);
            if (se.useThis.equals(id)) {
                if (! found) found = true;
                else return true;
            }
            i = i + 1;
        }
        return false;
    }

    /**************************************************************************
     * Disambiguating names. First, names are prepended with a type specific  *
     * string, defined in the array typePrefix. Then, the names are           *
     * considered in increasing type order. The first type in this order      *
     * are left unchanged. Then, each other order has a suffix appended to it *
     * until it is unique from all the current symbol names (both renamed and *
     * not yet renamed). The suffix is a prefix of "_context" where "context" *
     * is the context in which the name is defined (which is the empty string *
     * for the global context). If it is still not unique after all of the    *
     * context is appended, then the decimal representation of the name type  *
     * is added repeatedly until it is unambiguous.                           *
     **************************************************************************/
    public void Disambiguate ( ) {
        for (int vtype = 0; vtype <= num_vtypes; vtype++)
            for (int i = 0; i < symtab.size(); i++) {
                final SymTabEntry se = symtab.elementAt(i);
                if (se.type == vtype) {
                    se.useThis = typePrefix[vtype] + se.id;
                    int suffixLength = 0;
                    while (vtype > 0 && Ambiguous(se.useThis)) {
                        suffixLength = suffixLength + 1;
                        if (suffixLength == 1) se.useThis = se.useThis + "_";
                        else if (suffixLength > se.context.length() + 1)
                            se.useThis = se.useThis + vtype;
                        else se.useThis = se.useThis
                            + se.context.charAt(suffixLength - 2);
                    }
                    if (! se.id.equals(se.useThis))
                        disambiguateReport.addElement(
                        "\\* " +
                        vtypeName[se.type] +
                        " " +
                        se.id +
                        ((se.context.length() == 0)
                         ? "" 
                         : (" of " + se.cType + " " + se.context)) +
                        " at line " + se.line + " col " + se.col +
                        " changed to " +
                        se.useThis);
                }
            }
    }

    public String toString ( ) {
        int i = 0;
        String result = "[";
        while (i < symtab.size()) {
           final SymTabEntry se = symtab.elementAt(i);
            if (i > 0) result = result + ", ";
            result = result + vtypeName[se.type] + " " + se.context
                + ':' + se.id + " line " + se.line + " col " +
                se.col + " (" + se.useThis + ")";
            i = i + 1;
        }
        return result + "]";
    }

    /********************************
     * Type generic extract routine *
     ********************************/
    private void ExtractSym (final AST ast, final String context) {
        if (ast.getClass().equals(AST.UniprocessObj.getClass()))
            ExtractUniprocess((AST.Uniprocess) ast, context);
        else if (ast.getClass().equals(AST.MultiprocessObj.getClass()))
            ExtractMultiprocess((AST.Multiprocess) ast, context);
        else PcalDebug.ReportBug("Unexpected AST type.");
    }

    private void ExtractStmt (final AST ast, final String context, final String cType) {
         if (ast.getClass().equals(AST.WhileObj.getClass()))
            ExtractWhile((AST.While) ast, context, cType);
        else if (ast.getClass().equals(AST.AssignObj.getClass()))
            ExtractAssign((AST.Assign) ast, context, cType);
        else if (ast.getClass().equals(AST.IfObj.getClass()))
            ExtractIf((AST.If) ast, context, cType);
        else if (ast.getClass().equals(AST.WithObj.getClass()))
            ExtractWith((AST.With) ast, context, cType);
        else if (ast.getClass().equals(AST.WhenObj.getClass()))
            ExtractWhen((AST.When) ast, context, cType);
        else if (ast.getClass().equals(AST.PrintSObj.getClass()))
            ExtractPrintS((AST.PrintS) ast, context, cType);
        else if (ast.getClass().equals(AST.AssertObj.getClass()))
            ExtractAssert((AST.Assert) ast, context, cType);
        else if (ast.getClass().equals(AST.SkipObj.getClass()))
            ExtractSkip((AST.Skip) ast, context, cType);
        else if (ast.getClass().equals(AST.LabelIfObj.getClass()))
            ExtractLabelIf((AST.LabelIf) ast, context, cType);
        else if (ast.getClass().equals(AST.CallObj.getClass()))
            ExtractCall((AST.Call) ast, context, cType);
        else if (ast.getClass().equals(AST.ReturnObj.getClass()))
            ExtractReturn((AST.Return) ast, context, cType);
        else if (ast.getClass().equals(AST.CallReturnObj.getClass()))
            ExtractCallReturn((AST.CallReturn) ast, context, cType);
        else if (ast.getClass().equals(AST.CallGotoObj.getClass()))
            ExtractCallGoto((AST.CallGoto) ast, context, cType);
        else if (ast.getClass().equals(AST.GotoObj.getClass()))
            ExtractGoto((AST.Goto) ast, context, cType);

        /*******************************************************************
        * Handling of Either and LabelEither added by LL on 24 Jan 2006.   *
        *******************************************************************/
        else if (ast.getClass().equals(AST.EitherObj.getClass()))
            ExtractEither((AST.Either) ast, context, cType);
        else if (ast.getClass().equals(AST.LabelEitherObj.getClass()))
            ExtractLabelEither((AST.LabelEither) ast, context, cType);
        else PcalDebug.ReportBug("Unexpected AST type " + ast.toString());
    }


    /**********************************************
     * Type specific extraction routines.         *
     **********************************************/

    private void ExtractUniprocess (final AST.Uniprocess ast, final String context) {
// On 3 Feb 2006, LL moved insertion of "stack" before extraction of 
// global variable decls, so use of "stack" as a global variable would be 
// detected.
        if (ast.prcds.size() > 0) InsertSym(GLOBAL, "stack", "", "", 0, 0);
        for (int i = 0; i < ast.decls.size(); i++)
            ExtractVarDecl((AST.VarDecl) ast.decls.elementAt(i), "");
        for (int i = 0; i < ast.prcds.size(); i++)
            ExtractProcedure((AST.Procedure) ast.prcds.elementAt(i), "");
        if (ast.body.size() > 0) {
            final AST.LabeledStmt ls = (AST.LabeledStmt) ast.body.elementAt(0);
            iPC = ls.label;
        }
        for (int i = 0; i < ast.body.size(); i++) {
            ExtractLabeledStmt((AST.LabeledStmt) ast.body.elementAt(i), "", "");
        }
    }
        
    private void ExtractMultiprocess (final AST.Multiprocess ast, final String context) {
// On 3 Feb 2006, LL moved insertion of "stack" before extraction of 
// global variable decls, so use of "stack" as a global variable would be 
// detected.
        if (ast.prcds.size() > 0) InsertSym(GLOBAL, "stack", "", "", 0, 0);
        for (int i = 0; i < ast.decls.size(); i++)
            ExtractVarDecl((AST.VarDecl) ast.decls.elementAt(i), "");
        for (int  i = 0; i < ast.prcds.size(); i++)
            ExtractProcedure((AST.Procedure) ast.prcds.elementAt(i), "");
        for (int i = 0; i < ast.procs.size(); i++)
            ExtractProcess((AST.Process) ast.procs.elementAt(i), "");
    }

    private void ExtractProcedure (final AST.Procedure ast, final String context) {
        
        if (! InsertProc(ast))
            errorReport = errorReport + "\nProcedure " + ast.name +
            		" redefined at line " + ast.line + ", column " + ast.col;
        @SuppressWarnings("unused") final boolean b = InsertSym(PROCEDURE,
                      ast.name,
                      context,
                      "procedure",
                      ast.line,
                      ast.col);
        for (int i = 0; i < ast.decls.size(); i++)
            ExtractPVarDecl((AST.PVarDecl) ast.decls.elementAt(i), ast.name);
        for (int i = 0; i < ast.params.size(); i++)
            ExtractParamDecl((AST.PVarDecl) ast.params.elementAt(i), ast.name);
        for (int i = 0; i < ast.body.size(); i++)
            ExtractLabeledStmt((AST.LabeledStmt) ast.body.elementAt(i),
                               ast.name,
                               "procedure");
    }
        
    private void ExtractProcess(final AST.Process ast, final String context) {
        @SuppressWarnings("unused") final boolean b;
        if (! InsertProcess(ast))
            errorReport = errorReport + "\nProcess " + ast.name +
            		" redefined at line " + ast.line + ", column " + ast.col;
        b = InsertSym(PROCESS, ast.name, context, "process", ast.line, ast.col);
        for (int i = 0; i < ast.decls.size(); i++)
            ExtractVarDecl((AST.VarDecl) ast.decls.elementAt(i), ast.name);
        for (int i = 0; i < ast.body.size(); i++)
            ExtractLabeledStmt((AST.LabeledStmt) ast.body.elementAt(i),
                               ast.name,
                               "process");
    }
        
    private void ExtractVarDecl(final AST.VarDecl ast, final String context) {
        final int vtype = (context == "") ? GLOBAL : PROCESSVAR;
        if (! InsertSym(vtype, ast.var, context, "process", ast.line, ast.col))
            errorReport = errorReport + "\n" + vtypeName[vtype] + " " + ast.var +
            " redefined at line " + ast.line + ", column " + ast.col;
    }

    private void ExtractPVarDecl(final AST.PVarDecl ast, final String context) {
        if (! InsertSym(PROCEDUREVAR,
                        ast.var,
                        context,
                        "procedure",
                        ast.line,
                        ast.col))
            errorReport = errorReport + "\nProcedure variable " + ast.var +
            " redefined at line " + ast.line + ", column " + ast.col;
    }

    private void ExtractParamDecl(final AST.PVarDecl ast, final String context) {
        if (! InsertSym(PARAMETER,
                        ast.var,
                        context,
                        "procedure",
                        ast.line,
                        ast.col))
            errorReport = errorReport + "\nParameter " + ast.var +
            " redefined at line " + ast.line + ", column " + ast.col;
    }

    private void ExtractLabeledStmt(final AST.LabeledStmt ast,
                                    final String context,
                                    final String cType) {
        if (! InsertSym(LABEL, ast.label, context, cType, ast.line, ast.col))
            errorReport = errorReport + "\nLabel " + ast.label +
                " redefined at line " + ast.line + ", column " + ast.col;
        for (int i = 0; i < ast.stmts.size(); i++)
            ExtractStmt((AST) ast.stmts.elementAt(i), context, cType);
    }

    private void ExtractWhile(final AST.While ast, final String context, final String cType) {
        for (int i = 0; i < ast.unlabDo.size(); i++)
            ExtractStmt((AST) ast.unlabDo.elementAt(i), context, cType);
        for (int  i = 0; i < ast.labDo.size(); i++)
            ExtractLabeledStmt((AST.LabeledStmt) ast.labDo.elementAt(i),
                               context,
                               cType);
    }

    private void ExtractAssign(final AST.Assign ast, final String context, final String cType) {
    }

    private void ExtractIf(final AST.If ast, final String context, final String cType) {
        for (int i = 0; i < ast.Then.size(); i++)
            ExtractStmt((AST) ast.Then.elementAt(i), context, cType);
        for (int i = 0; i < ast.Else.size(); i++)
            ExtractStmt((AST) ast.Else.elementAt(i), context, cType);
    }

    private void ExtractWith(final AST.With ast, final String context, final String cType) {
        for (int i = 0; i < ast.Do.size(); i++)
            ExtractStmt((AST) ast.Do.elementAt(i), context, cType);
    }

    private void ExtractWhen(final AST.When ast, final String context, final String cType) {
    }

    private void ExtractPrintS(final AST.PrintS ast, final String context, final String cType) {
    }

    private void ExtractAssert(final AST.Assert ast, final String context, final String cType) {
    }

    private void ExtractSkip(final AST.Skip ast, final String context, final String cType) {
    }

    private void ExtractLabelIf(final AST.LabelIf ast, final String context, final String cType) {
        for (int i = 0; i < ast.unlabThen.size(); i++) 
            ExtractStmt((AST) ast.unlabThen.elementAt(i), context, cType);
        for (int i = 0; i < ast.labThen.size(); i++)
            ExtractLabeledStmt((AST.LabeledStmt) ast.labThen.elementAt(i),
                               context,
                               cType);
        for (int i = 0;  i < ast.unlabElse.size(); i++)
            ExtractStmt((AST) ast.unlabElse.elementAt(i), context, cType);
        for (int i = 0; i < ast.labElse.size(); i++)
            ExtractLabeledStmt((AST.LabeledStmt) ast.labElse.elementAt(i),
                               context,
                               cType);
    }

    private void ExtractCall(final AST.Call ast, final String context, final String cType) {
    }

    private void ExtractReturn(final AST.Return ast, final String context, final String cType) {
    }

    private void ExtractCallReturn(final AST.CallReturn ast,
                                   final String context,
                                   final String cType) {
    }

    private void ExtractCallGoto(final AST.CallGoto ast,
                                 final String context,
                                 final String cType) {
    }

    private void ExtractGoto(final AST.Goto ast, final String context, final String cType) {
    }

    /***********************************************************************
    * Handling of Either and LabelEither added by LL on 24 Jan 2006.       *
    ***********************************************************************/
    private void ExtractEither(final AST.Either ast, final String context, final String cType) {
        for (int i = 0; i < ast.ors.size(); i++)
              { @SuppressWarnings("unchecked") final Vector<AST> orClause = (Vector<AST>) ast.ors.elementAt(i) ;
                for (int j = 0; j < orClause.size(); j++)
                  ExtractStmt((AST) orClause.elementAt(j), context, cType);
               }
    }

    private void ExtractLabelEither(final AST.LabelEither ast, final String context,
                                    final String cType) {
        for (int i = 0; i < ast.clauses.size(); i++)
              { final AST.Clause orClause = (AST.Clause) ast.clauses.elementAt(i) ;
                for (int j = 0; j < orClause.unlabOr.size(); j++)
                  ExtractStmt((AST) orClause.unlabOr.elementAt(j), 
                                 context, cType);
                 for (int j = 0; j < orClause.labOr.size(); j++)
                   ExtractLabeledStmt((AST.LabeledStmt) 
                                          orClause.labOr.elementAt(j), 
                                       context, cType);
               }
    }

   /************************************************************************
   * Reports an error if "defaultInitValue" appears in the symbol table.   *
   * Added by LL on 31 Aug 2007.                                           *
   ************************************************************************/
   public void CheckForDefaultInitValue() throws PcalSymTabException {
     String errors = "" ;
     for (int i = 0 ; i < symtab.size() ; i++) 
       { final SymTabEntry se = symtab.elementAt(i);
         if (se.id.equals("defaultInitValue")) 
           { if (errors.equals(""))
               { errors = "Cannot use `defaultInitValue' as " ;}
             else {errors = errors + " or " ;}
               errors = errors + vtypeName[se.type] + " name";
           }
       }
       if (! errors.equals(""))
       { throw new PcalSymTabException(errors) ; }
       return ;
    }

}
