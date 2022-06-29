package pcal.exception;

import pcal.AST;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class PcalTranslateException extends UnrecoverablePositionedException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -5401952653536650870L;

	/**
     * @param message
     * @param elementAt2
     */
    public PcalTranslateException(String message, AST elementAt2)
    {
        super(message, elementAt2);
    }

    /**
     * @param message
     */
    public PcalTranslateException(String message)
    {
        super(message);
    }

}
