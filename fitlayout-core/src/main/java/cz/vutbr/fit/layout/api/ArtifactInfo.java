/**
 * ArtifactInfo.java
 *
 * Created on 2. 12. 2020, 16:37:47 by burgetr
 */
package cz.vutbr.fit.layout.api;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.BaseArtifact;

/**
 * Basic information about an artifact that can be obtained from a repository
 * without actually loading the whole artifact.
 * 
 * @author burgetr
 */
public class ArtifactInfo extends BaseArtifact
{
    private IRI artifactType;
    

    public ArtifactInfo(IRI parentIri)
    {
        super(parentIri);
    }

    @Override
    public IRI getArtifactType()
    {
        return artifactType;
    }

    public void setArtifactType(IRI artifactType)
    {
        this.artifactType = artifactType;
    }

    @Override
    public String toString()
    {
        String ret = String.valueOf(getIri());
        if (artifactType != null)
            ret += " [" + String.valueOf(artifactType) + "]";
        return ret;
    }
}
