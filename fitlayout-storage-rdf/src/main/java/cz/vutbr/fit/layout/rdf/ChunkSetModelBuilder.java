/**
 * ChunkSetBuilder.java
 *
 * Created on 10. 4. 2021, 20:35:29 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFBox;

/**
 * 
 * @author burgetr
 */
public class ChunkSetModelBuilder extends ModelBuilderBase implements ModelBuilder
{
    private ValueFactory vf;

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
        
        for (TextChunk chunk : cset.getTextChunks())
        {
            addChunk(chunk, csetIri, graph);
        }
        
        return graph;
    }
    
    private void addChunk(TextChunk chunk, IRI csetIri, Model graph)
    {
        final IRI ciri = getIriFactory().createTextChunkURI(csetIri, chunk);
        graph.add(ciri, RDF.TYPE, SEGM.TextChunk);
        if (chunk.getName() != null)
            graph.add(ciri, RDFS.LABEL, vf.createLiteral(chunk.getName()));
        graph.add(ciri, SEGM.belongsToChunkSet, csetIri);
        graph.add(ciri, SEGM.hasText, vf.createLiteral(chunk.getText()));
        
        if (chunk.getEffectiveBackgroundColor() != null)
        {
            graph.add(ciri, BOX.backgroundColor, vf.createLiteral(Serialization.colorString(chunk.getEffectiveBackgroundColor())));
        }
        
        final IRI areaIri;
        if (chunk.getSourceArea() instanceof RDFArea)
            areaIri = ((RDFArea) chunk.getSourceArea()).getIri();
        else
            areaIri = getIriFactory().createAreaURI(chunk.getSourceArea().getAreaTree().getIri(), chunk.getSourceArea());
        graph.add(ciri, SEGM.hasSourceArea, areaIri);
        
        final IRI boxIri;
        if (chunk.getSourceBox() instanceof RDFBox)
            boxIri = ((RDFBox) chunk.getSourceBox()).getIri();
        else
            boxIri = getIriFactory().createBoxURI(chunk.getSourceBox().getPageIri(), chunk.getSourceBox());
        graph.add(ciri, SEGM.hasSourceBox, boxIri);
    }
    
}
