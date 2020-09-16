/**
 * BaseService.java
 *
 * Created on 16. 9. 2020, 13:46:09 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.api.Service;
import cz.vutbr.fit.layout.api.ServiceManager;

/**
 * A base implementation of a service.
 *  
 * @author burgetr
 */
public abstract class BaseService implements Service
{
    private ServiceManager serviceManager;

    /**
     * Gets the service manager used by the service.
     * @return The service manager.
     */
    public ServiceManager getServiceManager()
    {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager)
    {
        this.serviceManager = serviceManager;
    }
    
}
