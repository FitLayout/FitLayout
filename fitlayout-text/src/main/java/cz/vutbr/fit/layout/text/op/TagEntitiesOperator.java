/**
 * TagEntitiesOperator.java
 *
 * Created on 22. 1. 2015, 16:02:09 by burgetr
 */
package cz.vutbr.fit.layout.text.op;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ScriptObject;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.text.tag.Tagger;
import cz.vutbr.fit.layout.text.tag.TreeTagger;


/**
 * 
 * @author burgetr
 */
public class TagEntitiesOperator extends BaseOperator implements ScriptObject
{
    private static Logger log = LoggerFactory.getLogger(TagEntitiesOperator.class);

    private TreeTagger tagger;
    private List<Tagger> usedTaggers;

    
    public TagEntitiesOperator()
    {
        usedTaggers = new ArrayList<>();
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
     * Registers a collection of taggers that should be used by this operator.
     * @param taggers the collection of tagger instances to be added
     */
    public void addTaggers(Collection<Tagger> taggers)
    {
        for (Tagger tagger : taggers)
            usedTaggers.add(tagger);
    }
    
    /**
     * Unregisters all taggers from the operator.
     */
    public void clearTaggers()
    {
        usedTaggers.clear();
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
        if (usedTaggers.isEmpty())
            log.warn("Applying TagEntitiesOperator with no taggers configured");
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
