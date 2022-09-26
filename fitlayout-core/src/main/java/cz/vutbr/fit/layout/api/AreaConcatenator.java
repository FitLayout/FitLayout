/**
 * AreaConcatenator.java
 *
 * Created on 26. 9. 2022, 9:45:10 by burgetr
 */
package cz.vutbr.fit.layout.api;

import cz.vutbr.fit.layout.model.Area;

/**
 * A concatenator that can convert a list of areas to a text string. Additionally,
 * it specifies a box concatenator to be used for concatenating boxes in leaf areas.
 * 
 * @author burgetr
 */
public interface AreaConcatenator extends Concatenator<Area>
{
    
    /**
     * Gets the box concatenator used for the leaf areas.
     * 
     * @return A box concatenator instance.
     */
    public BoxConcatenator getBoxConcatenator();

}
