/**
 * BaseProcessor.java
 *
 * Created on 5. 2. 2015, 9:34:28 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactRepository;
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
    //private static Logger log = LoggerFactory.getLogger(BaseProcessor.class);
    
    private ServiceManager serviceManager;
    

    /**
     * Creates the processor and configures to use a default (in-memory) artifact repository.
     */
    public BaseProcessor()
    {
        serviceManager = ServiceManager.createAndDiscover();
    }
    
    /**
     * Creates the processor and configures it to use the given artifact repository.
     * @param repository the artifact repository to be used by the processor
     */
    public BaseProcessor(ArtifactRepository repository)
    {
        serviceManager = ServiceManager.createAndDiscover();
        serviceManager.setArtifactRepository(repository);
    }
    
    /**
     * Gets the used instance of service manager for accessing the artifact services.
     * @return a service manager instance
     */
    public ServiceManager getServiceManager()
    {
        return serviceManager;
    }

    /**
     * Configures the processor to use a custom service manager.
     * @param serviceManager the service manager to use
     */
    public void setServiceManager(ServiceManager serviceManager)
    {
        this.serviceManager = serviceManager;
    }

    /**
     * Gets all the available artifact services used by the processor.
     * @return a map that maps service identifiers to the service implementations. 
     */
    public Map<String, ArtifactService> getArtifactServices()
    {
        return getServiceManager().findArtifactSevices();
    }

    /**
     * Gets the available artifact proiders of the given type used by the processor.
     * @param artifactType the type of the artifact
     * @return a map that maps service identifiers to the service implementations. 
     */
    public Map<String, ArtifactService> getArtifactProviders(IRI artifactType)
    {
        return getServiceManager().findArtifactProviders(artifactType);
    }

    /**
     * Gets all the available area tree operators used by the processor.
     * @return a map that maps service identifiers to the operator implementations. 
     */
    public Map<String, AreaTreeOperator> getOperators()
    {
        return getServiceManager().findAreaTreeOperators();
    }
    
    //======================================================================================================
    
    /**
     * Processes an input artifact and creates a new artifact by invoking an artifact service and its
     * configuration.
     * @param input the input artifact or {@code null} if the provider service does not require and input
     * artifact
     * @param provider the provider service to invoke
     * @param params the provider service configuration to use
     * @return the new artifact obtained from the provider
     */
    public Artifact processArtifact(Artifact input, ArtifactService provider, Map<String, Object> params)
    {
        if (provider instanceof ParametrizedOperation)
            getServiceManager().setServiceParams((ParametrizedOperation) provider, params);
        return provider.process(input);
    }
    
    /**
     * Applies an area tree operator to an area tree.
     * @param atree
     * @param op
     * @param params
     */
    public void apply(AreaTree atree, AreaTreeOperator op, Map<String, Object> params)
    {
        getServiceManager().setServiceParams(op, params);
        op.apply(atree);
    }

}
