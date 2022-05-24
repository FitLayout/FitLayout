/**
 * TagByExampleOperator.java
 *
 * Created on 24. 5. 2022, 20:02:54 by burgetr
 */
package cz.vutbr.fit.layout.map.op;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;

/**
 * 
 * @author burgetr
 */
public class TagByExampleOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(TagByExampleOperator.class);

    public TagByExampleOperator()
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
        return "Tags the occurrences of examples obtained from page metadata";
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
        
        final RDFArtifactRepository repo = (RDFArtifactRepository) getServiceManager().getArtifactRepository();
        final IRI pageIri = atree.getPageIri();
        final IRI metaIri = repo.getMetadataIRI(pageIri);
        var mapping = createMapping(repo, metaIri);
        log.info("Metadata context IRI: {}", metaIri);
        log.info("Mapping: {}", mapping);
        
        recursiveTagOcurrences(root, mapping);
    }

    private void recursiveTagOcurrences(Area root, Map<String, List<Example>> mapping)
    {
        final List<Example> examples = mapping.get(textFilter(root.getText()));
        if (examples != null)
        {
            for (Example ex : examples)
            {
                root.addTag(new DefaultTag(ex.getPredicate(), "Metadata", ex.getPredicate().getLocalName()), 0.9f);
            }
        }
        for (Area child : root.getChildren())
        {
            recursiveTagOcurrences(child, mapping);
        }
    }
    
    /**
     * Creates the mapping from metadata taken from the repository.
     * @param repo
     * @param metadataContextIri
     * @return
     */
    private Map<String, List<Example>> createMapping(RDFArtifactRepository repo, IRI metadataContextIri)
    {
        var gen = new MetadataExampleGenerator(repo, metadataContextIri);
        var allExamples = gen.getExamples();
        Map<String, List<Example>> ret = new HashMap<>();
        for (Example ex : allExamples)
        {
            final String text = textFilter(ex.getText());
            if (!text.isBlank())
            {
                List<Example> list = ret.get(text);
                if (list == null)
                {
                    list = new ArrayList<>(1);
                    ret.put(text, list);
                }
                list.add(ex);
            }
        }
        return ret;
    }
    
    private String textFilter(String src)
    {
        if (src == null)
            return "";
        else
            return src.toLowerCase().trim().replaceAll("\\s+", " ");
    }

}
