/**
 * BaseOperator.java
 *
 * Created on 21. 1. 2015, 10:12:31 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.api.AreaTreeOperator;

/**
 * A common base for our area operators.
 * 
 * @author burgetr
 */
public abstract class BaseOperator extends BaseParametrizedOperation implements AreaTreeOperator
{

    @Override
    public String toString()
    {
        return getName() + " (" + getId() + ")";
    }

}
