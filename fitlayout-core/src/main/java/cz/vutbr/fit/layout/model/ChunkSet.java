/**
 * ChunkSet.java
 *
 * Created on 8. 4. 2021, 13:28:54 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

/**
 * A set of text chunks extracted from a page.
 *  
 * @author burgetr
 */
public interface ChunkSet extends Artifact
{

    /**
     * Gets the IRI of the area tree the chunk set was build from. This may be equal
     * to the parent IRI when the chunk set was built from an area tree directly.
     * @return the source area tree IRI
     */
    public IRI getAreaTreeIri();
    
    /**
     * Gets the IRI of the related page.
     * @return The page IRI.
     */
    public IRI getPageIri();
    
    public Set<TextChunk> getTextChunks();
    
    public AreaTopology getTopology();
    
    public TextChunk createTextChunk(Rectangular r, Area sourceArea, Box sourceBox);
    
}
