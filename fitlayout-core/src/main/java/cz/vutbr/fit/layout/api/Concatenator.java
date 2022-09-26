/**
 * Concatenator.java
 *
 * Created on 26. 9. 2022, 9:41:50 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.List;

/**
 * A concatenator that can convert a list of elements to a text string.
 * 
 * @author burgetr
 */
public interface Concatenator<T>
{
    
    /**
     * Creates a text string by concatenating the contents of the given
     * elements.
     * @param elems The list of elements to concatenate.
     * @return Resulting text string.
     */
    public String concat(List<T> elems);

}
