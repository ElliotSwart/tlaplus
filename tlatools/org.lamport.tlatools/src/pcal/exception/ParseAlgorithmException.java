package pcal.exception;

import pcal.AST;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class ParseAlgorithmException extends UnrecoverablePositionedException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 3784651974781713851L;

	/**
     * @param message
     */
    public ParseAlgorithmException(String message)
    {
        super(message);
    }

    /**
     * @param string
     * @param elementAt
     */
    public ParseAlgorithmException(String message, AST elementAt)
    {
        super(message, elementAt);
    }
}
