/**
 * DefaultTag.java
 *
 * Created on 27. 11. 2014, 22:50:30 by burgetr
 */
package org.fit.layout.impl;

import org.fit.layout.model.Tag;

/**
 * A default simple tag implementation that allows to specify the tag value and level.
 * 
 * @author burgetr
 */
public class DefaultTag implements Tag
{
    private String value;
    private int level;
    private String type;

    public DefaultTag(String value)
    {
        this.value = value;
        this.level = 0;
        this.type = "";
    }

    public DefaultTag(String type, String value)
    {
        this.value = value;
        this.level = 0;
        this.type = type;
    }

    public DefaultTag(String value, int level)
    {
        this.value = value;
        this.level = level;
        this.type = "";
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        if (type == null)
        {
            if (other.getType() != null) return false;
        }
        else if (!type.equals(other.getType())) return false;
        if (value == null)
        {
            if (other.getValue() != null) return false;
        }
        else if (!value.equals(other.getValue())) return false;
        return true;
    }


    
}
