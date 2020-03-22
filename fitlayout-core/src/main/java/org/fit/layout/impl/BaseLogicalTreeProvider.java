/**
 * BaseLogicalTreeProvider.java
 *
 * Created on 19. 3. 2015, 13:56:41 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.api.LogicalTreeProvider;

/**
 * A common base for our logical tree providers.
 * 
 * @author burgetr
 */
public abstract class BaseLogicalTreeProvider extends BaseParametrizedOperation implements LogicalTreeProvider
{

    @Override
    public String toString()
    {
        return getName() + " (" + getId() + ")";
    }

}
