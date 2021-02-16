/**
 * StorageException.java
 *
 * Created on 16. 2. 2021, 18:15:24 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

/**
 * A generic exception that us used to wrap the exceptions thrown
 * by the underlying storage (rdf4j).
 * 
 * @author burgetr
 */
public class StorageException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public StorageException(String message)
    {
        super(message);
    }

    public StorageException(Throwable cause)
    {
        super(cause);
    }

}
