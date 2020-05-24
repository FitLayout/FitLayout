/**
 * GUIProcessor.java
 *
 * Created on 5. 2. 2015, 10:59:17 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class GUIProcessor extends ScriptableProcessor
{
    private static Logger log = LoggerFactory.getLogger(GUIProcessor.class);

    /**
     * Root nodes of the artifact trees - pages.
     */
    private List<Page> pages;
    
    private Vector<AreaTreeOperator> selectedOperators;
    private Vector<Map<String, Object>> operatorParams;
    
    public GUIProcessor()
    {
        super();
        pages = new ArrayList<>();
        selectedOperators = new Vector<AreaTreeOperator>();
        operatorParams = new Vector<Map<String, Object>>();
        loadConfig();
    }
    
    public List<Page> getPages()
    {
        return pages;
    }
    
    public void addPage(Page page)
    {
        pages.add(page);
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
        //TODO
    }
    
    //========================================================================================
    
}
