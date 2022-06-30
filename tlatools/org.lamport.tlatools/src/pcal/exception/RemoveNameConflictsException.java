package pcal.exception;

/**
 * @author Simon Zambrovski
 * @version $Id$
 */
public class RemoveNameConflictsException extends UnrecoverablePositionedException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -9110055310375572440L;

	/**
     * @param message
     */
    public RemoveNameConflictsException(final String message)
    {
        super(message);
    }

    /**
     * @param e2
     */
    public RemoveNameConflictsException(final UnrecoverablePositionedException e2)
    {
        super(e2.getMessage(), e2.getPosition());
    }

}
