/**
 * BaseBoxTreeProvider.java
 *
 * Created on 4. 2. 2015, 13:38:15 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.api.BoxTreeProvider;

/**
 * A common base for our box tree providers.
 * 
 * @author burgetr
 */
public abstract class BaseBoxTreeProvider extends BaseParametrizedOperation implements BoxTreeProvider
{

    @Override
    public String toString()
    {
        return getName() + " (" + getId() + ")";
    }

}
