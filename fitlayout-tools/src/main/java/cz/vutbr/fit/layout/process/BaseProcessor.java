/**
 * BaseProcessor.java
 *
 * Created on 5. 2. 2015, 9:34:28 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;

/**
 * A base of a processor. It takes care about the existing providers and operators and their invocation.
 * 
 * @author burgetr
 */
public abstract class BaseProcessor
{
    private static Logger log = LoggerFactory.getLogger(BaseProcessor.class);
    
    private ServiceManager serviceManager;
    

    public BaseProcessor()
    {
        serviceManager = ServiceManager.createAndDiscover();
    }
    
    public ServiceManager getServiceManager()
    {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager)
    {
        this.serviceManager = serviceManager;
    }

    public Map<String, ArtifactService> getArtifactServices()
    {
        return getServiceManager().findArtifactSevices();
    }

    public Map<String, ArtifactService> getArtifactProviders(IRI artifactType)
    {
        return getServiceManager().findArtifactProviders(artifactType);
    }

    public Map<String, AreaTreeOperator> getOperators()
    {
        return getServiceManager().findAreaTreeOperators();
    }
    
    //======================================================================================================
    
    public Artifact processArtifact(Artifact input, ArtifactService provider, Map<String, Object> params)
    {
        if (provider instanceof ParametrizedOperation)
            getServiceManager().setServiceParams((ParametrizedOperation) provider, params);
        return provider.process(input);
    }
    
    public void apply(AreaTree atree, AreaTreeOperator op, Map<String, Object> params)
    {
        if (atree != null)
        {
            getServiceManager().setServiceParams(op, params);
            op.apply(atree);
        }
        else
            log.error("Couldn't apply " + op.getId() + ": no area tree");
    }

    //======================================================================================================
    
    protected void treesCompleted()
    {
        //this is called when the tree creation is finished
    }
    

}