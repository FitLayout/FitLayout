/**
 * DefaultTag.java
 *
 * Created on 27. 11. 2014, 22:50:30 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.RESOURCE;

/**
 * A default simple tag implementation.
 * 
 * @author burgetr
 */
public class DefaultTag implements Tag
{
    private IRI iri;
    private String name;
    private String type;

    public DefaultTag(String name)
    {
        this.iri = createDefaultIri("x", name);
        this.name = name;
        this.type = "";
    }

    public DefaultTag(String type, String name)
    {
        this.iri = createDefaultIri(type, name);
        this.name = name;
        this.type = type;
    }

    public DefaultTag(IRI iri, String type, String name)
    {
        this.iri = iri;
        this.name = name;
        this.type = type;
    }

    public IRI getIri()
    {
        return iri;
    }

    public void setIri(IRI iri)
    {
        this.iri = iri;
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
        return name;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((iri == null) ? 0 : iri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DefaultTag other = (DefaultTag) obj;
        if (iri == null)
        {
            if (other.iri != null) return false;
        }
        else if (!iri.equals(other.iri)) return false;
        return true;
    }

    /**
     * Creates a default IRI for a tag when no IRI is provided in constructor.
     * 
     * @param type
     * @param name
     * @return
     */
    public IRI createDefaultIri(String type, String name)
    {
        return Values.iri(RESOURCE.NAMESPACE, "tag-" + type.replaceAll("\\.", "-") + "--" + name);
    }
    
}
