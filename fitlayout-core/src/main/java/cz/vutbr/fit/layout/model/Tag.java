/**
 * Tag.java
 *
 * Created on 17. 10. 2014, 11:36:39 by burgetr
 */
package cz.vutbr.fit.layout.model;

import org.eclipse.rdf4j.model.IRI;

/**
 * A tag that can be assigned to an area. The tag is uniquely identified
 * by its IRI. Moreoved, it has a name and a type tha allows to distinguish
 * tags of different purposes.
 * 
 * @author burgetr
 */
public interface Tag
{
    
    /**
     * A unique tag IRI.
     * @return
     */
    public IRI getIri();
    
    /**
     * Obtains the name of the tag.
     * @return the tag name
     */
    public String getName();

    /**
     * The tag type that allows to distinguish tags of different
     * purposes and different source. Each tag source should have
     * its own type.
     * @return the tag type string
     */
    public String getType();
    
}
