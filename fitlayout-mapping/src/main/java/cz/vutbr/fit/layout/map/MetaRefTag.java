/**
 * MetaRefTag.java
 *
 * Created on 29. 5. 2022, 14:29:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultTag;

/**
 * A tag that describes the reference to a metadata entry. 
 * 
 * @author burgetr
 */
public class MetaRefTag extends DefaultTag
{
    private Example example;

    public MetaRefTag(IRI iri, String name, Example example)
    {
        super(iri, "meta", name);
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

}
