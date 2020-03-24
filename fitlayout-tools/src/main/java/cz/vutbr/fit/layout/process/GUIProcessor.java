/**
 * GUIProcessor.java
 *
 * Created on 5. 2. 2015, 10:59:17 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.Map;
import java.util.Vector;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.AreaTreeProvider;
import cz.vutbr.fit.layout.api.LogicalTreeProvider;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class GUIProcessor extends ScriptableProcessor
{
    private static Logger log = LoggerFactory.getLogger(GUIProcessor.class);
    
    private Vector<AreaTreeOperator> selectedOperators;
    private Vector<Map<String, Object>> operatorParams;
    private boolean configMode = false;
    
    public GUIProcessor()
    {
        super();
        selectedOperators = new Vector<AreaTreeOperator>();
        operatorParams = new Vector<Map<String, Object>>();
        loadConfig();
    }
    
    public Vector<AreaTreeOperator> getSelectedOperators()
    {
        return selectedOperators;
    }

    public Vector<Map<String, Object>> getOperatorParams()
    {
        return operatorParams;
    }

    public void loadConfig()
    {
        configMode = true;
        try
        {
            execInternal("default_operators.js");
        } catch (ScriptException e) {
            log.error("Couldn't load config: " + e.getMessage());
        }
        configMode = false;
    }
    
    @Override
    public AreaTree segmentPage()
    {
        if (!getAreaProviders().isEmpty())
        {
            //just use the first available provider as the default
            AreaTreeProvider provider = getAreaProviders().values().iterator().next();
            log.warn("Using default area tree provider " + provider.getId());
            return segmentPage(provider, null);
        }
        else
            return null;
    }
    
    public AreaTree segmentPage(AreaTreeProvider provider, Map<String, Object> params)
    {
        setAreaTree(null);
        initAreaTree(provider, params);
        for (int i = 0; i < selectedOperators.size(); i++)
        {
            apply(selectedOperators.elementAt(i), operatorParams.elementAt(i));
        }
        treesCompleted();
        return getAreaTree();
    }
    
    @Override
    public LogicalAreaTree buildLogicalTree()
    {
        if (!getAreaProviders().isEmpty())
        {
            //just use the first available provider as the default
            LogicalTreeProvider provider = getLogicalProviders().values().iterator().next();
            log.warn("Using default logical area tree provider " + provider.getId());
            return buildLogicalTree(provider, null);
        }
        else
            return null;
    }
    
    public LogicalAreaTree buildLogicalTree(LogicalTreeProvider provider, Map<String, Object> params)
    {
        setLogicalAreaTree(null);
        initLogicalTree(provider, params);
        treesCompleted();
        return getLogicalAreaTree();
    }
    //========================================================================================
    
    @Override
    public Page renderPage(String providerName, Map<String, Object> params)
    {
        if (!configMode)
            return super.renderPage(providerName, params);
        else
            return getPage();
    }

    @Override
    public AreaTree initAreaTree(String providerName, Map<String, Object> params)
    {
        if (!configMode)
            return super.initAreaTree(providerName, params);
        else
            return getAreaTree();
    }

    @Override
    public void apply(String operatorName, Map<String, Object> params)
    {
        if (!configMode)
            super.apply(operatorName, params);
        else
        {
            AreaTreeOperator op = getOperators().get(operatorName);
            if (op != null)
            {
                ServiceManager.setServiceParams(op, params);
                selectedOperators.add(op);
                operatorParams.add(ServiceManager.getServiceParams(op));
            }
        }
    }
    
    
    
}
