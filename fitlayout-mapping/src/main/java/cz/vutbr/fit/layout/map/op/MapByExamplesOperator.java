/**
 * TagByExampleOperator.java
 *
 * Created on 24. 5. 2022, 20:02:54 by burgetr
 */
package cz.vutbr.fit.layout.map.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.ontology.MAPPING;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;

/**
 * 
 * @author burgetr
 */
public class MapByExamplesOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(MapByExamplesOperator.class);
    
    public MapByExamplesOperator()
    {
        super();
    }

    @Override
    public String getId()
    {
        return "FitLayout.Map.Examples";
    }
    
    @Override
    public String getName()
    {
        return "Map by examples";
    }

    @Override
    public String getDescription()
    {
        return "Maps the areas that correspond to the occurrences of examples obtained"
                + " from page metadata to the metadata individuals and properties";
    }

    @Override
    public String getCategory()
    {
        return "Mapping";
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
        if (!(atree instanceof RDFAreaTree && root instanceof RDFArea))
            throw new RuntimeException("This operator may be used with RDF artifacts only");
        
        final RDFArtifactRepository repo = (RDFArtifactRepository) getServiceManager().getArtifactRepository();
        final ValueFactory vf = repo.getStorage().getValueFactory();
        final IRI pageIri = atree.getPageIri();
        final IRI metaIri = repo.getMetadataIRI(pageIri);
        var gen = new MetadataExampleGenerator(repo, metaIri, MetadataExampleGenerator::normalizeText);
        var mapping = gen.getMappedExamples();
        log.info("Metadata context IRI: {}", metaIri);
        log.info("Mapping: {}", mapping);
        
        recursiveMapOcurrences((RDFAreaTree) atree, (RDFArea) root, mapping, vf);
    }

    private void recursiveMapOcurrences(RDFAreaTree atree, RDFArea root, Map<String, List<Example>> mapping, ValueFactory vf)
    {
        final String text = MetadataExampleGenerator.normalizeText(root.getText());
        final List<Example> examples = mapping.get(text);
        if (examples != null)
        {
            for (Example ex : examples)
            {
                atree.getAdditionalStatements().add(vf.createStatement(root.getIri(), MAPPING.describesInstance, ex.getSubject(), atree.getIri()));
                atree.getAdditionalStatements().add(vf.createStatement(root.getIri(), MAPPING.isValueOf, ex.getPredicate(), atree.getIri()));
            }
        }
        for (Area child : root.getChildren())
        {
            if (child instanceof RDFArea)
                recursiveMapOcurrences(atree, (RDFArea) child, mapping, vf);
        }
    }

}
