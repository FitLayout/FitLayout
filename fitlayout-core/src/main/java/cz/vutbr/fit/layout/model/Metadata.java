/**
 * Metadata.java
 *
 * Created on 20. 5. 2022, 13:53:26 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * A generic metadata definition.
 * 
 * @author burgetr
 */
public interface Metadata
{

    /**
     * Metadata type (e.g. MIME type)
     */
    public String getType();

    /**
     * Metadata content representation. The format depends on the metadata type.
     */
    public Object getContent();

}
