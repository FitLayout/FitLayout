/**
 * AreaTree.java
 *
 * Created on 14. 1. 2015, 15:16:42 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

/**
 * A tree of visual areas created from a box tree.
 * 
 * @author burgetr
 */
public interface AreaTree extends Artifact, SearchableAreaContainer
{

    /**
     * Gets the IRI of the page the area tree was build from. This may be equal
     * to the parent IRI when the tree was built from the page directly.
     * @return the source page IRI
     */
    public IRI getPageIri();
    
    /**
     * Obtains the root node of the area tree.
     * 
     * @return the root node of the tree of areas
     */
    public Area getRoot();

    /**
     * Updates the topology structures (e.g. grids) for all the areas in the tree.
     */
    public void updateTopologies();
 
    /**
     * Creates a new empty area in the tree.
     * @param r the new area bounds.
     * @return the new area
     */
    public Area createArea(Rectangular r);
    
    /**
     * Creates a new area in the tree from a box.
     * @param box source box
     * @return the new area containing the box.
     */
    public Area createArea(Box box);
    
    /**
     * Creates a new area in the tree by copying another area.
     * @param other the other area to copy
     * @return the new area containing the box.
     */
    public Area createArea(Area other);
    
    /**
     * Creates a new area in the tree from a list of boxes.
     * @param boxes a list of boxes
     * @return the new area containing all the boxes.
     */
    public Area createArea(List<Box> boxes);
    
}
