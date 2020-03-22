/**
 * BaseAreaTreeProvider.java
 *
 * Created on 4. 2. 2015, 13:39:57 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.api.AreaTreeProvider;

/**
 * A common base for our box tree providers.
 * 
 * @author burgetr
 */
public abstract class BaseAreaTreeProvider extends BaseParametrizedOperation implements AreaTreeProvider
{

    @Override
    public String toString()
    {
        return getName() + " (" + getId() + ")";
    }

}
