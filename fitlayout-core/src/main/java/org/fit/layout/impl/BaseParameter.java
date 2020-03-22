/**
 * BaseParameter.java
 *
 * Created on 1. 12. 2017, 13:59:55 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.api.Parameter;

/**
 * A common base of parameter specifications.
 *  
 * @author burgetr
 */
public class BaseParameter implements Parameter
{
    private String name;

    public BaseParameter(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

}
