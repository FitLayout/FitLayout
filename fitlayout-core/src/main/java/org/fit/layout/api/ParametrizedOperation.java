/**
 * ParametrizedOperation.java
 *
 * Created on 16. 1. 2015, 15:37:45 by burgetr
 */
package org.fit.layout.api;

import java.util.List;

/**
 * An implementation of an operation with external parametres of different types.
 * 
 * @author burgetr
 */
public interface ParametrizedOperation
{

    /**
     * Obtains a list of available parameters.
     * @return The list of parameters.
     */
    public List<Parameter> getParams();
    
    /**
     * Sets the value of the given parameter.
     * @param name parameter name
     * @param value parameter value
     * @return true when successfully set, false for unknown parameter or invalid value
     */
    public boolean setParam(String name, Object value);
    
    /**
     * Obtains the value of the given parameter.
     * @param name the parameter name
     * @return the parameter value or {@code null} for unknown parameter
     */
    public Object getParam(String name);
    
}
