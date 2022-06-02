/**
 * TagByExamplesOperator.java
 *
 * Created on 24. 5. 2022, 20:02:54 by burgetr
 */
package cz.vutbr.fit.layout.map.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetaRefTag;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.map.MetadataTagManager;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.model.RDFArea;

/**
 * Tags the areas that correspond to the occurrences of examples obtained from page metadata 
 * by the corresponding tags.
 * 
 * @author burgetr
 */
public class TagByExamplesOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(TagByExamplesOperator.class);
    
    public TagByExamplesOperator()
    {
        super();
    }

    @Override
    public String getId()
    {
        return "FitLayout.Tag.Examples";
    }
    
    @Override
    public String getName()
    {
        return "Tag by examples";
    }

    @Override
    public String getDescription()
    {
        return "Tags the areas that correspond to the occurrences of examples obtained"
                + " from page metadata by the corresponding tags";
    }

    @Override
    public String getCategory()
    {
        return "Classification";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        return ret;
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
        if (!(getServiceManager().getArtifactRepository() instanceof RDFArtifactRepository))
            throw new RuntimeException("This operator may be used with RDF artifact repository only");
        
        // get the annotated examples from metadata
        var repo = (RDFArtifactRepository) getServiceManager().getArtifactRepository();
        var metadataContextIri = repo.getMetadataIRI(atree.getPageIri());
        var gen = new MetadataExampleGenerator(repo, metadataContextIri, MetadataExampleGenerator::normalizeText);
        
        // get/create tags for examples
        var tagMgr = new MetadataTagManager(repo, metadataContextIri);
        var tags = tagMgr.checkForTags(gen.getExamples());
        Map<Example, MetaRefTag> assignedTags = new HashMap<>();
        for (var tag : tags)
            assignedTags.put(tag.getExample(), tag);

        var mapping = gen.getStringExamples();
        //log.info("Metadata context IRI: {}", metadataContextIri);
        //log.info("Mapping: {}", mapping);
        
        recursiveMapOcurrences(root, mapping, assignedTags);
    }

    private void recursiveMapOcurrences(Area root, Map<String, List<Example>> mapping, Map<Example, MetaRefTag> assignedTags)
    {
        final String text = MetadataExampleGenerator.normalizeText(root.getText());
        final List<Example> examples = mapping.get(text);
        if (examples != null)
        {
            for (Example example : examples)
            {
                final MetaRefTag tag = assignedTags.get(example);
                if (tag == null)
                    log.warn("No tag found for example {}", example);
                else
                    root.addTag(tag, 0.95f);
            }
        }
        for (Area child : root.getChildren())
        {
            if (child instanceof RDFArea)
                recursiveMapOcurrences(child, mapping, assignedTags);
        }
    }

}
