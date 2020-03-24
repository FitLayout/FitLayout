/**
 * AbstractPageSet.java
 *
 * Created on 1. 2. 2016, 23:51:22 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Date;

import cz.vutbr.fit.layout.api.PageSet;

/**
 * A base implementation of the page set that holds the basic necessary
 * information about the page set. The page loading itself is let on subclasses.
 * @author burgetr
 */
public abstract class AbstractPageSet implements PageSet 
{
    private String name;
    private String description;
    private Date dateCreated;
    
    
    public AbstractPageSet(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Date getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated;
    }

}
