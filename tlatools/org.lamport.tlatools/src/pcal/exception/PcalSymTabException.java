package pcal.exception;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class PcalSymTabException extends UnrecoverableException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3358103337832290610L;

	/**
     * @param message
     */
    public PcalSymTabException(final String message)
    {
        super(message);
    }

}
