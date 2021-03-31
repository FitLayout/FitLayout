/**
 * TagEntitiesOperator.java
 *
 * Created on 22. 1. 2015, 16:02:09 by burgetr
 */
package cz.vutbr.fit.layout.text.op;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ScriptObject;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.text.tag.Tagger;
import cz.vutbr.fit.layout.text.tag.TreeTagger;
import cz.vutbr.fit.layout.text.taggers.DateTagger;
import cz.vutbr.fit.layout.text.taggers.LocationsTagger;
import cz.vutbr.fit.layout.text.taggers.PersonsTagger;
import cz.vutbr.fit.layout.text.taggers.TimeTagger;


/**
 * 
 * @author burgetr
 */
public class TagEntitiesOperator extends BaseOperator implements ScriptObject
{
    private TreeTagger tagger;
    private Map<String, Tagger> availableTaggers;
    private List<Tagger> usedTaggers;

    
    public TagEntitiesOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Tag.Entities";
    }
    
    @Override
    public String getName()
    {
        return "Tag entities";
    }

    @Override
    public String getDescription()
    {
        return "Recognizes entities in area text using different taggers"
                + " and adds the corresponding tags to the areas.";
    }

    @Override
    public String getCategory()
    {
        return "classification";
    }

    /**
     * Registers a new tagger that should be used by this operator.
     * @param tagger the tagger instance to be added
     */
    public void addTagger(Tagger tagger)
    {
        usedTaggers.add(tagger);
    }
    
    /**
     * Unregisters all taggers from the operator.
     */
    public void clearTaggers()
    {
        usedTaggers.clear();
    }
    
    public Tagger findTagger(String id, Map<String, Object> params)
    {
        ParametrizedOperation op = availableTaggers.get(id);
        if (op != null)
            ServiceManager.setServiceParams(op, params);
        return (Tagger) op;
    }
    
    protected void initTaggers()
    {
        //availableTaggers = getServiceManager().loadServicesByType(Tagger.class);
        //usedTaggers = new ArrayList<Tagger>(availableTaggers.values());
        usedTaggers = new ArrayList<>();
        usedTaggers.add(new DateTagger());
        usedTaggers.add(new TimeTagger());
        usedTaggers.add(new PersonsTagger());
        usedTaggers.add(new LocationsTagger());
    }
    
    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        initTaggers();
        tagger = new TreeTagger(root);
        for (Tagger t : usedTaggers)
            tagger.addTagger(t);
        tagger.tagTree();
    }

    @Override
    public String getVarName()
    {
        return "entities";
    }

    @Override
    public void setIO(Reader in, Writer out, Writer err)
    {
    }

}
