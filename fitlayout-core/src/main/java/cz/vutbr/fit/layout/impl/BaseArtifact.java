/**
 * BaseArtifact.java
 *
 * Created on 23. 5. 2020, 19:02:37 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A common base class for all artifact implementations.
 * 
 * @author burgetr
 */
public abstract class BaseArtifact implements Artifact
{
    private String id;
    private Artifact parent;
    

    public BaseArtifact(Artifact parent)
    {
        this.parent = parent;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Artifact getParent()
    {
        return parent;
    }

    public void setParent(Artifact parent)
    {
        this.parent = parent;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BaseArtifact other = (BaseArtifact) obj;
        if (id == null)
        {
            if (other.id != null) return false;
        }
        else if (!id.equals(other.id)) return false;
        return true;
    }
    
}
