/**
 * Service.java
 *
 * Created on 15. 1. 2015, 14:59:10 by burgetr
 */
package cz.vutbr.fit.layout.api;

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
    
    /**
     * A category that allows to group similar services.
     * @return Category name or {@code null} when no category is assigned.
     */
    public String getCategory();
    
    /**
     * Assigns a service manager to the service. This is typically called by the service manager
     * itself during the service initialization.
     * @param manager The service manager to be set.
     */
    public void setServiceManager(ServiceManager manager);
    
}
