/**
 * ServiceException.java
 *
 * Created on 26. 9. 2020, 11:36:50 by burgetr
 */
package cz.vutbr.fit.layout.api;

/**
 * An exception that may occur during the service invocation or processing.
 * 
 * @author burgetr
 */
public class ServiceException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ServiceException()
    {
        super();
    }

    public ServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceException(String message)
    {
        super(message);
    }

    public ServiceException(Throwable cause)
    {
        super(cause);
    }
    
}
