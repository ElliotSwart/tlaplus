package pcal;

/**
 * Exception thrown instead of System.exit().
 * 
 * @author Simon Zambrovski
 * @version $Id$
 * @deprecated TODO this should be re-factored and not used further 
 */
public class PCalUnrecoverableErrorRuntimeException extends RuntimeException
{

    private static final long serialVersionUID = -7122672794272829880L;

	public PCalUnrecoverableErrorRuntimeException(final String message)
    {
        super(message);
    }

}
