/**
 * ModelBuilderBase.java
 *
 * Created on 30. 10. 2020, 16:35:44 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.FL;

/**
 * Common model builder methods.
 * 
 * @author burgetr
 */
public class ModelBuilderBase
{

    /**
     * Stores the common information about an artifact to a model.
     * 
     * @param graph the model to store the data to
     * @param a the artifact
     */
    public void addArtifactData(Model graph, Artifact a)
    {
        final ValueFactory vf = SimpleValueFactory.getInstance();
        
        final IRI node = a.getIri();
        graph.add(node, RDF.TYPE, a.getArtifactType());
        if (a.getParentIri() != null)
            graph.add(node, FL.hasParentArtifact, a.getParentIri());
        if (a.getLabel() != null)
            graph.add(node, RDFS.LABEL, vf.createLiteral(a.getLabel()));
        if (a.getCreatedOn() != null)
            graph.add(node, FL.createdOn, vf.createLiteral(a.getCreatedOn()));
        if (a.getCreator() != null)
            graph.add(node, FL.creator, vf.createLiteral(a.getCreator()));
        if (a.getCreatorParams() != null)
            graph.add(node, FL.creatorParams, vf.createLiteral(a.getCreatorParams()));
    }
    
}
