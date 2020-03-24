/**
 * AreaTreeProvider.java
 *
 * Created on 15. 1. 2015, 14:51:56 by burgetr
 */
package cz.vutbr.fit.layout.api;

import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * This interface represents a service that is able to provide a basic area tree from the given page.
 * 
 * @author burgetr
 */
public interface AreaTreeProvider extends Service, ParametrizedOperation
{

    /**
     * Creates a basic area tree from the given page. The details depend on the particular algorithm.
     * The obtained tree may be further processed by the area tree operators.
     * 
     * @param page the page to be processed
     * @return a tree of areas
     */
    public AreaTree createAreaTree(Page page);
    
}
