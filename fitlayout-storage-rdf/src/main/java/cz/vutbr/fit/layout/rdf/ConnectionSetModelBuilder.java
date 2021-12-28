/**
 * ConnectionSetModelBuilder.java
 *
 * Created on 27. 12. 2021, 19:41:27 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ConnectionSet;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFConnectionSet;

/**
 * 
 * @author burgetr
 */
public class ConnectionSetModelBuilder extends ModelBuilderBase implements ModelBuilder
{

    public ConnectionSetModelBuilder(IRIFactory iriFactory)
    {
        super(iriFactory);
    }

    @Override
    public Model createGraph(Artifact artifact)
    {
        return createModel((ConnectionSet) artifact, artifact.getIri());
    }
    
    //=========================================================================

    private Model createModel(ConnectionSet cset, IRI csetIri) 
    {
        final Model graph = new LinkedHashModel();
        
        addArtifactData(graph, cset);
        graph.add(csetIri, BOX.hasSource, cset.getSourceIri());
        if (cset.getPageIri() != null)
            graph.add(csetIri, SEGM.hasSourcePage, cset.getPageIri());
        
        addConnections(cset.getSourceIri(), cset.getAreaConnections(), graph);
        
        // additional RDF properties
        if (cset instanceof RDFConnectionSet)
        {
            final Set<Statement> toadd = ((RDFConnectionSet) cset).getAdditionalStatements();
            if (toadd != null)
                graph.addAll(toadd);
        }
        
        return graph;
    }

    private void addConnections(IRI sourceIri, Set<AreaConnection> conns, Model graph)
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
            final IRI relationIri = getIriFactory().createRelationURI(con.getRelation());
            graph.add(iri1, relationIri, iri2);
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
