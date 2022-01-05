/**
 * ServiceManager.java
 *
 * Created on 26. 2. 2015, 22:59:16 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultArtifactRepository;
import cz.vutbr.fit.layout.model.Artifact;

/**
 * This class provides access to registered services. It holds the instances of the available
 * services and their configuration. 
 * 
 * @author burgetr
 */
public class ServiceManager
{
    private static ServiceManager globalInstance;
    
    /** The used artifact repository */
    private ArtifactRepository artifactRepository;
    
    private Map<String, ArtifactService> artifactServices;
    private Map<String, AreaTreeOperator> operators;
    
    /** All the parametrized services */
    private Map<String, ParametrizedOperation> parametrizedServices;
    /** All the script objects (may coexist with other service types) */
    private Map<String, ScriptObject> scriptObjects;

    //===============================================================================================
    
    public static ServiceManager instance()
    {
        if (globalInstance == null)
            globalInstance = createAndDiscover();
        return globalInstance;
    }
    
    /**
     * Creates a new service manager which is initialized by the automatic service discovery.
     * 
     * @return A ServiceManager instance
     */
    public static ServiceManager createAndDiscover() 
    {
        ServiceManager mgr = new ServiceManager();
        mgr.initAndDiscover();
        return mgr;
    }
    
    protected void initAndDiscover()
    {
        artifactRepository = new DefaultArtifactRepository();
        scriptObjects = new HashMap<>();
        parametrizedServices = new HashMap<>();
        //load services of standard types
        artifactServices = loadServicesByType(ArtifactService.class);
        operators = loadServicesByType(AreaTreeOperator.class);
        //load the remaining script objects - this should be the last step
        loadScriptObjects();
    }
    
    /**
     * Creates a new empty service manager with no services.
     * 
     * @return A ServiceManager instance
     */
    public static ServiceManager create()
    {
        ServiceManager mgr = new ServiceManager();
        mgr.initEmpty();
        return mgr;
    }
    
    protected void initEmpty()
    {
        artifactRepository = new DefaultArtifactRepository();
        scriptObjects = new HashMap<>();
        parametrizedServices = new HashMap<>();
        //empty service lists
        artifactServices = new HashMap<>();
        operators = new HashMap<>();
    }
    
    /**
     * Gets the artifact repository currently used by the services. By default, an instance of {@link DefaultArtifactRepository} is used.
     * @return The artifact repository.
     */
    public ArtifactRepository getArtifactRepository()
    {
        return artifactRepository;
    }
    
    /**
     * Changes the artifact repository used by the services.
     * @param repository the repository to be used
     */
    public void setArtifactRepository(ArtifactRepository repository)
    {
        artifactRepository = repository;
    }
    
    //===============================================================================================
    
    /**
     * Adds a new artifact service to the manager.
     * @param op The service to add.
     */
    public void addArtifactService(ArtifactService op)
    {
        addTypedOperation(op, artifactServices);
    }
    
    /**
     * Discovers all the ArtifactService implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public Map<String, ArtifactService> findArtifactSevices()
    {
        return artifactServices;
    }
    
    /**
     * Discovers all the ArtifactService implementations that produce a given artifact type.
     * @param artifactType the artifact type to produce 
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public Map<String, ArtifactService> findArtifactProviders(IRI artifactType)
    {
        return artifactServices.entrySet().stream()
                .filter(x -> artifactType.equals(x.getValue().getProduces()))
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }
    
    /**
     * Adds a new area tree operator to the manager.
     * @param op The operator to add.
     */
    public void addAreaTreeOperator(AreaTreeOperator op)
    {
        addTypedOperation(op, operators);
    }
    
    /**
     * Discovers all the AreaTreeOperator service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public Map<String, AreaTreeOperator> findAreaTreeOperators()
    {
        return operators;
    }

    /**
     * Discovers all the ScriptObject service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public Map<String, ScriptObject> findScriptObjects()
    {
        return scriptObjects;
    }

    /**
     * Discovers the registered services of the given class.
     * @param clazz the class of the required services
     * @return A map that maps the services to their identifiers
     */
    public <T extends Service> Map<String, T> loadServicesByType(Class<T> clazz)
    {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        Iterator<T> it = loader.iterator();
        Map<String, T> ret = new HashMap<String, T>();
        while (it.hasNext())
        {
            T op = it.next();
            addTypedOperation(op, ret);
        }
        return ret;
    }

    private Map<String, ScriptObject> loadScriptObjects()
    {
        ServiceLoader<ScriptObject> loader = ServiceLoader.load(ScriptObject.class);
        Iterator<ScriptObject> it = loader.iterator();
        while (it.hasNext())
        {
            ScriptObject op = it.next();
            addScriptObject(op.getVarName(), op);
        }
        return scriptObjects;
    }
    
    //=============================================================================================
    
    /**
     * Configures and invokes an artifact service for an input artifact.
     *  
     * @param serviceId the ID of the service to be invoked
     * @param params A map of service input parametres (depending on the given service)
     * @param inputArtifact The input artifact to apply the service on (may be {@code null} for services that
     * do not use input artifacts, e.g. page rendering) 
     * @return The created output artifact.
     * @throws IllegalArgumentException when the service with the given ID is not available
     */
    public Artifact applyArtifactService(String serviceId, Map<String, Object> params, Artifact inputArtifact)
    {
        ParametrizedOperation op = findParmetrizedService(serviceId);
        
        if (op == null)
            throw new IllegalArgumentException("No such service: " + serviceId);
        
        if (!(op instanceof ArtifactService))
            throw new IllegalArgumentException("Not an ArtifactService: " + serviceId);
        
        if (params != null)
            ServiceManager.setServiceParams(op, params);
        
        return ((ArtifactService) op).process(inputArtifact);
    }
    
    /**
     * Sets the operation parametres based on a map of values.
     * @param op The operation whose parametres should be set
     * @param params A map that assigns values to parameter names
     */
    public static void setServiceParams(ParametrizedOperation op, Map<String, Object> params)
    {
        if (params != null)
        {
            for (Map.Entry<String, Object> entry : params.entrySet())
            {
                op.setParam(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Obtains the values of all the parametres of the given operation.
     * @param op The operation whose parametres should be set
     * @return A map that assigns values to parameter names
     */
    public static Map<String, Object> getServiceParams(ParametrizedOperation op)
    {
        Map<String, Object> ret = new HashMap<String, Object>();
        for (Parameter param : op.getParams())
        {
            ret.put(param.getName(), op.getParam(param.getName()));
        }
        return ret;
    }

    /**
     * Gets a map of all available parametrized operations.
     * @return a map from service ID to the service instance
     */
    public Map<String, ParametrizedOperation> getParametrizedServices()
    {
        return parametrizedServices;
    }
    
    /**
     * Finds a parametrized service based on its ID.
     * @param id the service ID.
     * @return the parametrized operation object or {@code null} when the service does not exist.
     */
    public ParametrizedOperation findParmetrizedService(String id)
    {
        if (parametrizedServices == null)
            return null;
        else
            return parametrizedServices.get(id);
    }
    
    /**
     * Adds a new service to the manager.
     * @param op a service to add. It must implement a ParametrizedOperation or a ScriptObject interface
     * to be handled properly.
     */
    public void addService(Service op)
    {
        op.setServiceManager(this);
        if (op instanceof ParametrizedOperation)
            addParametrizedService(op.getId(), (ParametrizedOperation) op);
        if (op instanceof ScriptObject)
            addScriptObject(((ScriptObject) op).getVarName(), (ScriptObject) op);
    }
    
    /**
     * Adds an operation to a corresponding map and updates the ParametrizedOperation and ScriptObject maps
     * when necessary.
     * @param <T> Operation tyoe
     * @param op the operation to add
     * @param dest the destination map to add to.
     */
    private <T extends Service> void addTypedOperation(T op, Map<String, T> dest)
    {
        addService(op);
        dest.put(op.getId(), op);
    }
    
    /**
     * Adds a new parametrized operation to the list of all parametrized operations.
     * @param id
     * @param op
     */
    private void addParametrizedService(String id, ParametrizedOperation op)
    {
        parametrizedServices.put(id, op);
    }
    
    /**
     * Adds a new script object to the manager.
     * @param id
     * @param op
     */
    public void addScriptObject(String id, ScriptObject op)
    {
        if (!scriptObjects.containsKey(id))
            scriptObjects.put(id, op);
    }
    
    /**
     * Finds a service in a collection of services based on its class.
     * @param services the collection of services to scan
     * @param clazz the required class of the service
     * @return the fisrt service in the collection that is instance of the given class or {@code null} when
     * no such servise is present in the collection. 
     */
    public <T> T findByClass(Collection<?> services, Class<T> clazz)
    {
        for (Object serv : services)
        {
            if (clazz.isInstance(serv))
                return clazz.cast(serv);
        }
        return null;
    }
    
}
