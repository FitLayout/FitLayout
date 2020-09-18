/**
 * GUIProcessor.java
 *
 * Created on 5. 2. 2015, 10:59:17 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.Map;
import java.util.Vector;

import cz.vutbr.fit.layout.api.AreaTreeOperator;

/**
 * 
 * @author burgetr
 */
public class GUIProcessor extends ScriptableProcessor
{
    //private static Logger log = LoggerFactory.getLogger(GUIProcessor.class);

    private Vector<AreaTreeOperator> selectedOperators;
    private Vector<Map<String, Object>> operatorParams;
    
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
        //TODO
    }
    
    //========================================================================================
    
}
