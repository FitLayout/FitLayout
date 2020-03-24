/**
 * LogicalTreeProvider.java
 *
 * Created on 19. 3. 2015, 13:32:40 by burgetr
 */
package cz.vutbr.fit.layout.api;

import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.LogicalAreaTree;

/**
 * This interface represents a service that is able to provide a logical area tree from the given area tree.
 * 
 * @author burgetr
 */
public interface LogicalTreeProvider extends Service, ParametrizedOperation
{
    
    /**
     * Creates a logical area tree from the given tree of areas. The details depend on the particular algorithm.
     * 
     * @param areaTree the source tree of areas
     * @return a tree of logical areas
     */
    public LogicalAreaTree createLogicalTree(AreaTree areaTree);

}
