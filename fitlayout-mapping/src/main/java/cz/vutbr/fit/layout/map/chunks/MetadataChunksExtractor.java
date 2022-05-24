/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.map.Example;
import cz.vutbr.fit.layout.map.MetadataExampleGenerator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.text.chunks.ChunksSource;

/**
 * 
 * @author burgetr
 */
public class MetadataChunksExtractor extends ChunksSource
{
    private RDFArtifactRepository repo;

    public MetadataChunksExtractor(Area root, RDFArtifactRepository repo)
    {
        super(root);
        this.repo = repo;
    }

    @Override
    public List<TextChunk> getTextChunks()
    {
        List<TextChunk> ret = new ArrayList<>();
        List<Example> examples = getExamples();
        for (Example ex : examples)
        {
            System.out.println(ex);
        }
        return ret;
    }
    
    private List<Example> getExamples()
    {
        final IRI pageIri = getRoot().getPageIri();
        final IRI metaIri = repo.getMetadataIRI(pageIri);
        var gen = new MetadataExampleGenerator(repo, metaIri);
        return gen.getExamples();
    }
    
}
