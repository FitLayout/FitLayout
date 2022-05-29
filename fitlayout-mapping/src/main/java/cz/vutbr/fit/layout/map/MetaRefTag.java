/**
 * MetaRefTag.java
 *
 * Created on 29. 5. 2022, 14:29:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import cz.vutbr.fit.layout.impl.DefaultTag;

/**
 * A tag that describes the reference to a metadata entry. 
 * 
 * @author burgetr
 */
public class MetaRefTag extends DefaultTag
{
    private Example example;

    public MetaRefTag(String name, Example example)
    {
        super("meta", name);
        this.example = example;
    }

    public Example getExample()
    {
        return example;
    }

    @Override
    public String toString()
    {
        return super.toString() + "#" + example.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((example == null) ? 0 : example.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        MetaRefTag other = (MetaRefTag) obj;
        if (example == null)
        {
            if (other.example != null) return false;
        }
        else if (!example.equals(other.example)) return false;
        return true;
    }

}
