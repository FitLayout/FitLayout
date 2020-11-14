/**
 * StreamConsumer.java
 *
 * Created on 14. 11. 2020, 22:10:42 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer.impl;

import java.io.InputStream;

/**
 * A generic thread that consumes an output stream of the backend.
 * 
 * @author burgetr
 */
public abstract class StreamConsumer implements Runnable
{
    private InputStream stream;
    private Object result;
    
    
    public StreamConsumer(InputStream stream)
    {
        this.stream = stream;
    }
    
    public Object getResult()
    {
        return result;
    }

    @Override
    public void run()
    {
        result = consume(stream);
    }

    public abstract Object consume(InputStream stream);
}
