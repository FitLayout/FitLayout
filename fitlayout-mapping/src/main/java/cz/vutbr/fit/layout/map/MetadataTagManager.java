/**
 * MetadataTagManager.java
 *
 * Created on 1. 6. 2022, 15:27:57 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.ontology.MAPPING;
import cz.vutbr.fit.layout.ontology.RESOURCE;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;

/**
 * Creates and reads the metadata mapping tags in the given context (subgraph).
 * 
 * @author burgetr
 */
public class MetadataTagManager
{
    private static Logger log = LoggerFactory.getLogger(MetadataTagManager.class);
    
    private RDFArtifactRepository repo;
    private IRI contextIri;
    private String tagIriBase; 
    
    public MetadataTagManager(RDFArtifactRepository repo, IRI contextIri)
    {
        this.repo = repo;
        this.contextIri = contextIri;
        tagIriBase = contextIri.getLocalName();
    }
    
    /**
     * Reads metadata tag definitions from the configured context.
     * 
     * @return A collection of metadata tags (possibly empty when no tags are defined)
     * @throws StorageException
     */
    public Collection<MetaRefTag> getMetaTags() throws StorageException
    {
        try {
            final String query = repo.getIriDecoder().declarePrefixes()
                    + "SELECT ?tag ?name ?type ?subj ?subjType ?pred WHERE { "
                    + " GRAPH <" + contextIri + "> {"
                    + "    ?tag segm:name ?name . "
                    + "    ?tag segm:type ?type . "
                    + "    ?tag map:describesInstance ?subj . "
                    + "    ?tag map:isValueOf ?pred . "
                    + "    ?tag rdf:type segm:Tag . "
                    + "    OPTIONAL { ?subj rdf:type ?subjType } "
                    + "}}";
            
            List<BindingSet> data = repo.getStorage().executeSafeTupleQuery(query);
            List<MetaRefTag> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                final Binding bIri = binding.getBinding("tag");
                final Binding bName = binding.getBinding("name");
                final Binding bType = binding.getBinding("type");
                final Binding bSubj = binding.getBinding("subj");
                final Binding bSubjType = binding.getBinding("subjType");
                final Binding bPred = binding.getBinding("pred");
                if (bIri != null && bName != null && bType != null && bSubj != null && bPred != null 
                        && bIri.getValue() instanceof IRI
                        && bSubj.getValue() instanceof Resource
                        && bPred.getValue() instanceof IRI)
                {
                    Example ex = new Example((Resource) bSubj.getValue(), (IRI) bPred.getValue(), "");
                    if (bSubjType != null && bSubjType.getValue() instanceof IRI)
                        ex.setSubjectType((IRI) bSubjType.getValue());
                    MetaRefTag tag = new MetaRefTag((IRI) bIri.getValue(), bName.getValue().stringValue(), ex);
                    ret.add(tag);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Creates a tag for every example.
     * 
     * @param examples a collection of examples
     * @return a collection of tags containing one tag for each example
     */
    public Collection<MetaRefTag> createTagsForExamples(Collection<Example> examples)
    {
        int tagIdCnt = 1;
        List<MetaRefTag> tags = new ArrayList<>();
        for (var example : examples)
        {
            final var tagName = example.getPredicate().getLocalName() + tagIdCnt;
            final var tagIri = Values.iri(RESOURCE.NAMESPACE, tagIriBase + "-tag-meta--" + tagName); 
            final var tag = new MetaRefTag(tagIri, tagName, example);
            tags.add(tag);
            tagIdCnt++;
        }
        return tags;
    }
    
    /**
     * Stores tags to the repository.
     * 
     * @param tags
     * @throws StorageException
     */
    public void storeTags(Collection<MetaRefTag> tags) throws StorageException
    {
        final ValueFactory vf = repo.getStorage().getValueFactory();
        Model graph = new LinkedHashModel();
        for (var tag : tags)
        {
            graph.add(vf.createStatement(tag.getIri(), RDF.TYPE, SEGM.Tag));
            graph.add(vf.createStatement(tag.getIri(), SEGM.type, vf.createLiteral(tag.getType())));
            graph.add(vf.createStatement(tag.getIri(), SEGM.name, vf.createLiteral(tag.getName())));
            graph.add(vf.createStatement(tag.getIri(), MAPPING.describesInstance, tag.getExample().getSubject()));
            graph.add(vf.createStatement(tag.getIri(), MAPPING.isValueOf, tag.getExample().getPredicate()));
        }
        repo.getStorage().insertGraph(graph, contextIri);
    }
    
    /**
     * Checks whether the context contains tags for examples. If not, the tags
     * are created from the given examples.
     * 
     * @param examples
     * @return the tags found or created
     */
    public Collection<MetaRefTag> checkForTags(Collection<Example> examples)
    {
        var tags = getMetaTags();
        if (tags.isEmpty())
        {
            final var newTags = createTagsForExamples(examples);
            storeTags(newTags);
            log.info("Created new tags in {} : {}", contextIri, newTags);
            return newTags;
        }
        else
        {
            log.info("Tags already present in {} : {}", contextIri, tags);
            return tags;
        }
    }

}
