/**
 * ChunksCache.java
 *
 * Created on 6. 12. 2018, 13:13:32 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * A cache of already created chunks depending on tags and hints.
 * 
 * @author burgetr
 */
public class ChunksCache
{
    private Map<TagSpec, List<TextChunk>> chunks;
    private int reads = 0;
    private int hits = 0;
    
    public ChunksCache()
    {
        chunks = new HashMap<>();
    }
    
    public void put(Tag tag, List<PresentationHint> hints, List<TextChunk> chunkAreas)
    {
        final TagSpec key = new TagSpec(tag, hints);
        chunks.put(key, chunkAreas);
    }
    
    public List<TextChunk> get(Tag tag, List<PresentationHint> hints)
    {
        final TagSpec key = new TagSpec(tag, hints);
        final List<TextChunk> ret = chunks.get(key);
        reads++;
        if (ret != null)
            hits++;
        return ret;
    }
    
    public int size()
    {
        return chunks.size();
    }
    
    public int getReads()
    {
        return reads;
    }

    public int getHits()
    {
        return hits;
    }
    
    public int getChunkCount()
    {
        int r = 0;
        for (List<TextChunk> list : chunks.values())
            r += list.size();
        return r;
    }

    //==========================================================================================
    
    private static class TagSpec
    {
        private Tag tag;
        private List<PresentationHint> hints;
        
        public TagSpec(Tag tag, List<PresentationHint> hints)
        {
            this.tag = tag;
            this.hints = hints;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((hints == null) ? 0 : hints.hashCode());
            result = prime * result + ((tag == null) ? 0 : tag.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            TagSpec other = (TagSpec) obj;
            if (hints == null)
            {
                if (other.hints != null) return false;
            }
            else if (!hints.equals(other.hints)) return false;
            if (tag == null)
            {
                if (other.tag != null) return false;
            }
            else if (!tag.equals(other.tag)) return false;
            return true;
        }
        
    }
    
}
