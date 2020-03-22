/**
 * ServiceManager.java
 *
 * Created on 26. 2. 2015, 22:59:16 by burgetr
 */
package org.fit.layout.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.fit.layout.gui.BrowserPlugin;

/**
 * This class provides static methods for managing the global services.
 * 
 * @author burgetr
 */
public class ServiceManager
{
    private static List<BrowserPlugin> browserPlugins;
    private static Map<String, BoxTreeProvider> boxProviders;
    private static Map<String, AreaTreeProvider> areaProviders;
    private static Map<String, LogicalTreeProvider> logicalProviders;
    private static Map<String, AreaTreeOperator> operators;
    private static Map<String, PageStorage> pageStorages;
    
    /** All the parametrized services */
    private static Map<String, ParametrizedOperation> parametrizedServices;
    /** All the script objects (may coexist with other service types) */
    private static Map<String, ScriptObject> scriptObjects;

    static {
        scriptObjects = new HashMap<String, ScriptObject>();
        parametrizedServices = new HashMap<String, ParametrizedOperation>();
        //load services of standard types
        browserPlugins = loadBrowserPlugins();
        boxProviders = loadServicesByType(BoxTreeProvider.class);
        areaProviders = loadServicesByType(AreaTreeProvider.class);
        logicalProviders = loadServicesByType(LogicalTreeProvider.class);
        operators = loadServicesByType(AreaTreeOperator.class);
        pageStorages = loadServicesByType(PageStorage.class);
        //load the remaining script objects - this should be the last step
        loadScriptObjects();
    }
    
    /**
     * Discovers all the BrowserPlugin service implementations.
     * @return A list of all browser plugins.
     */
    public static List<BrowserPlugin> findBrowserPlugins()
    {
        return browserPlugins;
    }    
    
    /**
     * Discovers all the BoxTreeProvider service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, BoxTreeProvider> findBoxTreeProviders()
    {
        return boxProviders;
    }
    
    /**
     * Discovers all the AreaTreeProvider service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, AreaTreeProvider> findAreaTreeProviders()
    {
        return areaProviders;
    }
    
    /**
     * Discovers all the LogicalTreeProvider service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, LogicalTreeProvider> findLogicalTreeProviders()
    {
        return logicalProviders;
    }
    
    /**
     * Discovers all the AreaTreeOperator service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, AreaTreeOperator> findAreaTreeOperators()
    {
        return operators;
    }

    /**
     * Discovers all the ScriptObject service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, ScriptObject> findScriptObjects()
    {
        return scriptObjects;
    }

    /**
     * Discovers all the PageStorage service implementations.
     * @return A map that assigns the service {@code id} to the appropriate implementation.
     */
    public static Map<String, PageStorage> findPageStorages()
    {
        return pageStorages;
    }

    /**
     * Discovers the registered services of the given class.
     * @param clazz the class of the required services
     * @return A map that maps the services to their identifiers
     */
    public static <T extends Service> Map<String, T> loadServicesByType(Class<T> clazz)
    {
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        Iterator<T> it = loader.iterator();
        Map<String, T> ret = new HashMap<String, T>();
        while (it.hasNext())
        {
            T op = it.next();
            ret.put(op.getId(), op);
            if (op instanceof ParametrizedOperation)
                addParametrizedService(op.getId(), (ParametrizedOperation) op);
            if (op instanceof ScriptObject)
                addScriptObject(((ScriptObject) op).getVarName(), (ScriptObject) op);
        }
        return ret;
    }
    
    private static List<BrowserPlugin> loadBrowserPlugins()
    {
        ServiceLoader<BrowserPlugin> loader = ServiceLoader.load(BrowserPlugin.class);
        Iterator<BrowserPlugin> it = loader.iterator();
        List<BrowserPlugin> ret = new ArrayList<BrowserPlugin>();
        while (it.hasNext())
        {
            BrowserPlugin plugin = it.next();
            ret.add(plugin);
            if (plugin instanceof ScriptObject)
                addScriptObject(((ScriptObject) plugin).getVarName(), (ScriptObject) plugin);
        }
        return ret;
    }
    
    private static Map<String, ScriptObject> loadScriptObjects()
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
     * Finds a parametrized service based on its ID.
     * @param id the service ID.
     * @return the parametrized operation object or {@code null} when the service does not exist.
     */
    public static ParametrizedOperation findParmetrizedService(String id)
    {
        if (parametrizedServices == null)
            return null;
        else
            return parametrizedServices.get(id);
    }
    
    /**
     * Adds a new parametrized operation to the list of all parametrized operations.
     * @param id
     * @param op
     */
    private static void addParametrizedService(String id, ParametrizedOperation op)
    {
        parametrizedServices.put(id, op);
    }
    
    private static void addScriptObject(String id, ScriptObject op)
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
    public static <T> T findByClass(Collection<?> services, Class<T> clazz)
    {
        for (Object serv : services)
        {
            if (clazz.isInstance(serv))
                return clazz.cast(serv);
        }
        return null;
    }
    
}
