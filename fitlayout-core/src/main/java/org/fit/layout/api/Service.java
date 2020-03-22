/**
 * Service.java
 *
 * Created on 15. 1. 2015, 14:59:10 by burgetr
 */
package org.fit.layout.api;

/**
 * A generic service with its ID, name and description.
 * 
 * @author burgetr
 */
public interface Service
{
    
    /** Obtains a unique ID of the service */
    public String getId();
    
    /**
     * Obtains a descriptive name of the service that may be presented to the user.
     * @return the operator name
     */
    public String getName();
    
    /**
     * Obtains a longer description of the service.
     * @return the description
     */
    public String getDescription();
    

}
