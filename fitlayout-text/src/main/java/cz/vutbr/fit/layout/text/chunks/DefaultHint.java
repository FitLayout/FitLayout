/**
 * DefaultHint.java
 *
 * Created on 6. 11. 2018, 13:47:56 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.model.TextChunk;


/**
 * A default no-op presentation hint.
 * @author burgetr
 */
public class DefaultHint implements PresentationHint
{
    private String name;
    private float support;
    private boolean inline;
    private boolean block;
    
    public DefaultHint(String name, float support)
    {
        this.name = name;
        this.support = support;
        this.inline = false;
        this.block = false;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean impliesInline()
    {
        return inline;
    }

    protected void setInline(boolean inline)
    {
        this.inline = inline;
    }

    @Override
    public boolean impliesBlock()
    {
        return block;
    }

    protected void setBlock(boolean block)
    {
        this.block = block;
    }

    public float getSupport()
    {
        return support;
    }

    @Override
    public SourceBoxList extractBoxes(Area a, SourceBoxList current, Set<Area> processed)
    {
        //no changes are performed 
        return current;
    }

    @Override
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences)
    {
        //no changes are performed
        return occurrences;
    }

    @Override
    public List<TextChunk> processChunks(Area src, List<TextChunk> chunks)
    {
        //no changes are performed
        return chunks;
    }

    @Override
    public List<TextChunk> postprocessChunks(List<TextChunk> src)
    {
        //no changes are performed
        return src;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        DefaultHint other = (DefaultHint) obj;
        return name.equals(other.name);
    }
    
}
