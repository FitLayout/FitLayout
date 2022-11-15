/**
 * MetadataDef.java
 *
 * Created on 20. 5. 2022, 13:50:00 by burgetr
 */
package cz.vutbr.fit.layout.json.parser;

import cz.vutbr.fit.layout.model.Metadata;

/**
 * A generic metadata definition.
 * 
 * @author burgetr
 */
public class MetadataDef implements Metadata
{
    /**
     * Metadata type (e.g. MIME type)
     */
    public String type;
    
    /**
     * Metadata representation. The format depends on the metadata type;
     */
    public Object content;

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
    public Object getContent()
    {
        return content;
    }

    public void setContent(Object content)
    {
        this.content = content;
    }
}
