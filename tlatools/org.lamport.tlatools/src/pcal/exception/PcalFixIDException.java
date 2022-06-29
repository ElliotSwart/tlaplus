package pcal.exception;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class PcalFixIDException extends UnrecoverableException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 6901771658852290251L;

	/**
     * @param message
     */
    public PcalFixIDException(String message)
    {
        super(message);
    }

}
