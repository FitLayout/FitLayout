/**
 * PageSet.java
 *
 * Created on 7. 1. 2016, 17:53:42 by burgetr
 */
package org.fit.layout.api;

import java.util.Date;

import org.fit.layout.model.Page;

/**
 * A named set of pages processed together.
 * 
 * @author burgetr
 */
public interface PageSet extends Iterable<Page>
{

    /**
     * Obtains the name of the set.
     * @return the set name
     */
    public String getName();

    /**
     * Obtains the set description.
     * @return the description (possibly empty)
     */
    public String getDescription();

    /**
     * Obtains the creation date of the set.
     * @return the date
     */
    public Date getDateCreated();
    
    /**
     * Obtains the number of pages contained in this set.
     * @return the page number
     */
    public int size();
    
    /**
     * Adds a new page to the set.
     * @param page The page to be added.
     */
    public void addPage(Page page);
    
    /**
     * Obtains the page at the given index.
     * @param index
     * @return
     */
    public Page get(int index) throws IndexOutOfBoundsException;
    
}
