/**
 * BaseTagger.java
 *
 * Created on 17. 2. 2016, 18:03:44 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.impl.BaseParametrizedOperation;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.segm.AreaStyle;

/**
 * A base implementation of a tagger with no parametres.
 * @author burgetr
 */
public abstract class BaseTagger extends BaseParametrizedOperation implements Tagger
{

    @Override
    public String toString()
    {
        return getId();
    }

    @Override
    public String getCategory()
    {
        // we don't use categories for taggers at the moment
        return null;
    }

    /**
     * Checks whether an area is homogeneous regarding the contents style and layout
     * so that it can be treated as an atomic entity for tagging.
     * @param a The area to test. 
     * @return {@code true} when the area is homogeneous.
     */
    protected boolean isHomogeneous(Area a)
    {
        //TODO consider layout? (e.g. separators)
        return a.isLeaf() || AreaStyle.hasConsistentStyle(a);
    }
    
}
