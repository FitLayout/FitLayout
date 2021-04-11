/**
 * IRIFactory.java
 *
 * Created on 30. 3. 2021, 18:25:35 by burgetr
 */

package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;

public interface IRIFactory
{

    /**
     * Creates a page set IRI from its name.
     * @param name the name of the page set
     * @return the created IRI
     */
    public IRI createPageSetURI(String name);

    /**
     * Creates an IRI of an artifact from its sequence number.
     * @param seq
     * @return
     */
    public IRI createArtifactIri(long seq);

    /**
     * Creates an IRI if an artifact from its id.
     * @param id
     * @return
     */
    public IRI createArtifactIri(String id);

    /**
     * Creates a box IRI.
     * @param pageUri
     * @param box
     * @return
     */
    public IRI createBoxURI(IRI pageUri, Box box);

    /**
     * Decodes a box ID from its IRI
     * @param boxIri
     * @return
     */
    public int decodeBoxId(IRI boxIri);
    
    /**
     * Creates a bouds rectange IRI
     * @param boxUri the corresponding box IRI
     * @param type bounds type used to distinguish different recatngles assigned to a single box (e.g. "v" for visual)
     * @return
     */
    public IRI createBoundsURI(IRI boxUri, String type);

    /**
     * Creates an IRI of a border description.
     * @param boxUri the corresponding box IRI
     * @param side the side ("top", "right", "bottom", "left") 
     * @return
     */
    public IRI createBorderURI(IRI boxUri, String side);

    /**
     * Creates an IRI of an HTML attribute description.
     * @param boxUri the corresponding box IRI
     * @param name attribute name
     * @return
     */
    public IRI createAttributeURI(IRI boxUri, String name);

    /**
     * Creates a visual area IRI.
     * @param areaTreeUri the uri of the owning area tree.
     * @param area the area to create the IRI for
     * @return
     */
    public IRI createAreaURI(IRI areaTreeUri, Area area);

    /**
     * Decodes an area ID from its IRI
     * @param areaIri
     * @return
     */
    public int decodeAreaId(IRI areaIri);
    
    /**
     * Creates a logical area IRI.
     * @param areaTreeUri the uri of the owning logical tree.
     * @param cnt the logical area ID
     * @return
     */
    public IRI createLogicalAreaURI(IRI areaTreeUri, int cnt);

    /**
     * Creates an IRI for a tag support assignment description.
     * @param areaUri
     * @param tag
     * @return
     */
    public IRI createTagSupportURI(IRI areaUri, Tag tag);

    /**
     * Creates an IRI of a tag.
     * @param tag
     * @return
     */
    public IRI createTagURI(Tag tag);

    /**
     * Creates a text chunk IRI.
     * @param chunkSetUri the IRI of the owning chunk set
     * @param chunk the text chunk itself
     * @return the created IRI
     */
    public IRI createTextChunkURI(IRI chunkSetUri, TextChunk chunk);
    
    /**
     * Decodes a text chunk id from its IRI.
     * @param textChunkIri
     * @return
     */
    public int decodeTextChunkId(IRI textChunkIri);

    /**
     * Creates a sequence IRI from its name.
     * @param name the name of the sequence (alphabetical characters only)
     * @return the created IRI
     */
    public IRI createSequenceURI(String name);

}