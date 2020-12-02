/**
 * BaseArtifact.java
 *
 * Created on 23. 5. 2020, 19:02:37 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.Date;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Artifact;

/**
 * A common base class for artifact implementations.
 * 
 * @author burgetr
 */
public abstract class BaseArtifact implements Artifact
{
    private IRI iri;
    private IRI parentIri;
    private String label;
    private Date createdOn;
    private String creator;
    private String creatorParams;
    

    public BaseArtifact(IRI parentIri)
    {
        this.parentIri = parentIri;
        createdOn = new Date();
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

    @Override
    public void setIri(IRI iri)
    {
        this.iri = iri;
    }

    @Override
    public IRI getParentIri()
    {
        return parentIri;
    }

    public void setParentIri(IRI parentIri)
    {
        this.parentIri = parentIri;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Date getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn)
    {
        this.createdOn = createdOn;
    }

    public String getCreator()
    {
        return creator;
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public String getCreatorParams()
    {
        return creatorParams;
    }

    public void setCreatorParams(String creatorParams)
    {
        this.creatorParams = creatorParams;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iri == null) ? 0 : iri.hashCode());
        result = prime * result
                + ((parentIri == null) ? 0 : parentIri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BaseArtifact other = (BaseArtifact) obj;
        if (iri == null)
        {
            if (other.iri != null) return false;
        }
        else if (!iri.equals(other.iri)) return false;
        if (parentIri == null)
        {
            if (other.parentIri != null) return false;
        }
        else if (!parentIri.equals(other.parentIri)) return false;
        return true;
    }

}
