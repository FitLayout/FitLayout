/**
 * ScriptObject.java
 *
 * Created on 28. 4. 2015, 16:38:19 by burgetr
 */
package org.fit.layout.api;

import java.io.Reader;
import java.io.Writer;

/**
 * An object that should be made available in the JavaScript engine.
 *  
 * @author burgetr
 */
public interface ScriptObject
{

    /**
     * Gets the name of the object in the JavaScript engine.
     * @return the JS identifier
     */
    public String getVarName();
    
    /**
     * Sets the readers/writers to be used by the script.
     * @param in input reader
     * @param out standard output writer
     * @param err error output writer
     */
    public void setIO(Reader in, Writer out, Writer err);

}
