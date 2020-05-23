/**
 * BaseArtifact.java
 *
 * Created on 23. 5. 2020, 19:02:37 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A common base class for all artifact implementations.
 * @author burgetr
 */
public abstract class BaseArtifact
{
    private Artifact parent;
    

    public BaseArtifact(Artifact parent)
    {
        this.parent = parent;
    }

    public Artifact getParent()
    {
        return parent;
    }

    public void setParent(Artifact parent)
    {
        this.parent = parent;
    }
}
