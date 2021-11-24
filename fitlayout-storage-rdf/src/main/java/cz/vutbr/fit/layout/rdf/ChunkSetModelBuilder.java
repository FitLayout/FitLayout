/**
 * ChunkSetBuilder.java
 *
 * Created on 10. 4. 2021, 20:35:29 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFChunkSet;

/**
 * 
 * @author burgetr
 */
public class ChunkSetModelBuilder extends ModelBuilderBase implements ModelBuilder
{
    private ValueFactory vf;
    private int next_order;

    public ChunkSetModelBuilder(IRIFactory iriFactory)
    {
        super(iriFactory);
        vf = SimpleValueFactory.getInstance();
    }

    @Override
    public Model createGraph(Artifact artifact)
    {
        return createModel((ChunkSet) artifact, artifact.getIri());
    }

    //=========================================================================
    
    private Model createModel(ChunkSet cset, IRI csetIri) 
    {
        final Model graph = new LinkedHashModel();
        final IRI areaTreeNode = cset.getAreaTreeIri();
        
        addArtifactData(graph, cset);
        graph.add(csetIri, SEGM.hasAreaTree, areaTreeNode);
        next_order = 0;
        
        for (TextChunk chunk : cset.getTextChunks())
        {
            addChunk(chunk, csetIri, graph);
        }
        
        // additional RDF properties
        if (cset instanceof RDFChunkSet)
        {
            final Set<Statement> toadd = ((RDFChunkSet) cset).getAdditionalStatements();
            if (toadd != null)
                graph.addAll(toadd);
        }
        
        return graph;
    }
    
    private void addChunk(TextChunk chunk, IRI csetIri, Model graph)
    {
        final IRI ciri = getTextChunkIri(csetIri, chunk);
        graph.add(ciri, RDF.TYPE, SEGM.TextChunk);
        if (chunk.getName() != null)
            graph.add(ciri, RDFS.LABEL, vf.createLiteral(chunk.getName()));
        graph.add(ciri, BOX.documentOrder, vf.createLiteral(next_order++));
        graph.add(ciri, SEGM.belongsToChunkSet, csetIri);
        graph.add(ciri, SEGM.text, vf.createLiteral(chunk.getText()));
        
        if (chunk.getEffectiveBackgroundColor() != null)
        {
            graph.add(ciri, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(chunk.getEffectiveBackgroundColor())));
        }
        
        final IRI areaIri = getAreaIri(chunk.getSourceArea().getAreaTree().getIri(), chunk.getSourceArea());
        graph.add(ciri, SEGM.hasSourceArea, areaIri);
        final IRI boxIri = getBoxIri(chunk.getSourceBox().getPageIri(), chunk.getSourceBox());
        graph.add(ciri, SEGM.hasSourceBox, boxIri);
        
        // append the geometry
        insertBounds(ciri, BOX.bounds, "b", chunk.getBounds(), graph);
        
        // append tags
        if (chunk.getTags().size() > 0) 
        {
            final Map<Tag, Float> tags = chunk.getTags();
            for (Tag t : tags.keySet()) 
            {
                Float support = tags.get(t);
                if (support != null && support > 0.0f)
                {
                    final IRI tagUri = getIriFactory().createTagURI(t);
                    graph.add(ciri, SEGM.hasTag, tagUri);
                    final IRI supUri = getIriFactory().createTagSupportURI(ciri, t);
                    graph.add(ciri, SEGM.tagSupport, supUri);
                    graph.add(supUri, SEGM.support, vf.createLiteral(support));
                    graph.add(supUri, SEGM.hasTag, tagUri);
                }
            }
        }

    }
    
}
