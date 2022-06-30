package pcal.exception;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class PcalResourceFileReaderException extends UnrecoverableException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1426704702132443049L;

	/**
     * @param message
     */
    public PcalResourceFileReaderException(final String message)
    {
        super(message);
    }

}
