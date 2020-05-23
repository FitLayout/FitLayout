/**
 * LogicalAreaTree.java
 *
 * Created on 19. 3. 2015, 13:29:30 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * A tree of logical areas.
 * 
 * @author burgetr
 */
public interface LogicalAreaTree extends Artifact
{
    
    /**
     * Obtains the source area tree used for creating this logical tree.
     * 
     * @return the source area tree
     */
    public AreaTree getAreaTree();
    
    /**
     * Obtains the root node of the logical area tree.
     * 
     * @return the root node of the tree of logical areas
     */
    public LogicalArea getRoot();


}
