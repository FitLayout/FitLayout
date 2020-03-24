/**
 * BaseParameter.java
 *
 * Created on 1. 12. 2017, 13:59:55 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.api.Parameter;

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
