/**
 * PageListener.java
 *
 * Created on 7. 2. 2016, 21:11:43 by burgetr
 */
package org.fit.layout.gui;

import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Page;

/**
 * The listener that should be informed when some tree is updated in the browser.
 * @author burgetr
 */
public interface TreeListener
{

    /**
     * Called when the page has been rendered and the box tree has been updated.
     * @param page the new page tree
     */
    public void pageRendered(Page page);
    
    /**
     * Called when the area tree has been updated.
     * @param tree the new area tree
     */
    public void areaTreeUpdated(AreaTree tree);
    
    /**
     * Called when the logical area tree has been updated.
     * @param tree the new logical area tree
     */
    public void logicalAreaTreeUpdated(LogicalAreaTree tree);
    
}
