/**
 * GUIProcessor.java
 *
 * Created on 5. 2. 2015, 10:59:17 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.model.AreaTree;

/**
 * 
 * @author burgetr
 */
public class GUIProcessor extends BaseProcessor
{
    //private static Logger log = LoggerFactory.getLogger(GUIProcessor.class);

    private List<AreaTreeOperator> selectedOperators;
    private List<Map<String, Object>> operatorParams;
    
    public GUIProcessor(ArtifactRepository repository)
    {
        super(repository);
        selectedOperators = new ArrayList<AreaTreeOperator>();
        operatorParams = new ArrayList<Map<String, Object>>();
        loadConfig();
    }
    
    public List<AreaTreeOperator> getSelectedOperators()
    {
        return selectedOperators;
    }

    public List<Map<String, Object>> getOperatorParams()
    {
        return operatorParams;
    }
    
    public AreaTree applyOperators(AreaTree src)
    {
        //TODO
        return null;
    }

    public void loadConfig()
    {
        //TODO
    }
    
    //========================================================================================
    
}
