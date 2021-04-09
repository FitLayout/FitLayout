/**
 * HintStyle.java
 *
 * Created on 10. 7. 2018, 15:37:07 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.api.TagResolver;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * This hint forces the chunk source to accept only the chunk tags that are assigned 
 * by a given resolver. The intrinsic tags that are not resolved by the resolver
 * are removed from the chunks.
 * 
 * @author burgetr
 */
public class HintForceResolved extends DefaultHint
{
    private Tag tag;
    private TagResolver dis;
    

    public HintForceResolved(Tag tag, TagResolver dis, float support)
    {
        super("Style", support);
        this.tag = tag;
        this.dis = dis;
    }

    public Tag getTag()
    {
        return tag;
    }

    public TagResolver getTagResolver()
    {
        return dis;
    }
    
    @Override
    public List<TextChunk> postprocessChunks(List<TextChunk> areas)
    {
        List<TextChunk> ret = new ArrayList<>(areas.size());
        for (TextChunk a : areas)
        {
            if (a instanceof TextChunk)
            {
                final Set<Tag> dtags = dis.getAreaTags(((TextChunk) a).getSourceArea());
                if (a.hasTag(tag) && !dtags.contains(tag)) //TODO tag support?
                {
                    a.removeTag(tag);
                    a.setName("!" + a.getName());
                }
                else
                {
                    ret.add(a);
                }
            }
        }
        return ret;
    }
    
    @Override
    public String toString()
    {
        return "Style:" + dis;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dis == null) ? 0 : dis.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        HintForceResolved other = (HintForceResolved) obj;
        if (dis == null)
        {
            if (other.dis != null) return false;
        }
        else if (!dis.equals(other.dis)) return false;
        return true;
    }
    
}
