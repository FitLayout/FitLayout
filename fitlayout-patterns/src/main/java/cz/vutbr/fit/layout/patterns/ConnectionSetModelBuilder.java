/**
 * ConnectionSetModelBuilder.java
 *
 * Created on 27. 12. 2021, 19:41:27 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.IRIFactory;
import cz.vutbr.fit.layout.rdf.ModelBuilderBase;

/**
 * 
 * @author burgetr
 */
public class ConnectionSetModelBuilder extends ModelBuilderBase
{

    public ConnectionSetModelBuilder(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    public Model createModel(IRI artifactIri, Collection<AreaConnection> conns) 
    {
        final Model graph = new LinkedHashModel();
        addConnections(artifactIri, conns, graph);
        return graph;
    }

    private void addConnections(IRI sourceIri, Collection<AreaConnection> conns, Model graph)
    {
        for (AreaConnection con : conns)
            addConnection(sourceIri, con, graph);
    }
    
    private void addConnection(IRI artifactIri, AreaConnection con, Model graph)
    {
        IRI iri1 = getContentRectIRI(artifactIri, con.getA1());
        IRI iri2 = getContentRectIRI(artifactIri, con.getA2());
        if (iri1 != null && iri2 != null)
        {
            // add a plain relation between the rectangles
            final IRI relationIri = getIriFactory().createRelationURI(con.getRelation());
            graph.add(iri1, relationIri, iri2);
            // add a relation description including support
            final IRI descrIri = getIriFactory().createRelationDescriptionURI(iri1, iri2, con.getRelation());
            graph.add(iri1, SEGM.isInRelation, descrIri);
            graph.add(descrIri, SEGM.hasRelatedRect, iri2);
            graph.add(descrIri, SEGM.hasRelationType, relationIri);
            graph.add(descrIri, SEGM.support, getValueFactory().createLiteral(con.getWeight()));
        }
    }
    
    private IRI getContentRectIRI(IRI artifactIri, ContentRect rect)
    {
        if (rect instanceof Area)
            return getAreaIri(artifactIri, (Area) rect);
        else if (rect instanceof TextChunk)
            return getTextChunkIri(artifactIri, (TextChunk) rect);
        else
            return null;
    }
    
}
