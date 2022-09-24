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
    private String description;

    public BaseParameter(String name)
    {
        this.name = name;
        this.description = "";
    }

    public BaseParameter(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

}
