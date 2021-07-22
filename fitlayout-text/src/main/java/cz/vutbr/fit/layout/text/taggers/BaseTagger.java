/**
 * BaseTagger.java
 *
 * Created on 17. 2. 2016, 18:03:44 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import cz.vutbr.fit.layout.impl.BaseParametrizedOperation;
import cz.vutbr.fit.layout.text.tag.Tagger;

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
    
}
