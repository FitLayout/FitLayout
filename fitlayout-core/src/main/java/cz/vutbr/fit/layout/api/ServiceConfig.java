/**
 * ServiceConfig.java
 *
 * Created on 7. 1. 2022, 18:00:46 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Map;

/**
 * A service configuration containing a service ID and parameter values.
 * 
 * @author burgetr
 */
public class ServiceConfig
{
    private String serviceId;
    private Map<String, Object> params;
    
    public ServiceConfig(String serviceId, Map<String, Object> params)
    {
        this.serviceId = serviceId;
        this.params = params;
    }

    public String getServiceId()
    {
        return serviceId;
    }

    public Map<String, Object> getParams()
    {
        return params;
    }

}
