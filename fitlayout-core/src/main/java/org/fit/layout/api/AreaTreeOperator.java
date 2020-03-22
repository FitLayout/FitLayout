/**
 * AreaTreeOperator.java
 *
 * Created on 24. 10. 2013, 9:46:04 by burgetr
 */
package org.fit.layout.api;

import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;

/**
 * A generic procedure that processes the area tree. The procedures may be applied in any order.
 * @author burgetr
 */
public interface AreaTreeOperator extends Service, ParametrizedOperation
{
    
    /**
     * Returns the operator category that allows to group similar operators in the GUI.
     * @return The category name
     */
    public String getCategory();
    
    /**
     * Applies the operation to the given tree.
     * @param atree the area tree to be modified.
     */
    public void apply(AreaTree atree);
    
    /**
     * Applies the operation to the given subtree of the tree.
     * @param atree the area tree to be modified.
     * @param root the root node of the affected subtree.
     */
    public void apply(AreaTree atree, Area root);
    
    
}
