/**
 * TaggedChunksSource.java
 *
 * Created on 9. 3. 2018, 23:37:25 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * An area list source that creates text chunks by extracting tagged chunks from leaf areas.
 * 
 * @author burgetr
 */
public class TaggedChunksSource extends ChunksSource
{
    private static Logger log = LoggerFactory.getLogger(TaggedChunksSource.class);
            
    private int idcnt;
    private float minTagSupport;
    private List<TextChunk> chunks;
    private TaggerConfig tagConfig;
    
    
    public TaggedChunksSource(TaggerConfig tagConfig, Area root, float minTagSupport)
    {
        super(root);
        idcnt = 1;
        this.minTagSupport = minTagSupport;
        this.tagConfig = tagConfig;
    }

    @Override
    public List<TextChunk> getTextChunks()
    {
        if (chunks == null)
        {
            chunks = new ArrayList<>();
            recursiveScan(getRoot(), chunks);
        }
        return chunks;
    }

    private void recursiveScan(Area root, List<TextChunk> dest)
    {
        if (root.isLeaf())
        {
            Set<Tag> supportedTags = root.getSupportedTags(minTagSupport);
            if (!supportedTags.isEmpty())
            {
                for (Tag t : supportedTags)
                {
                    List<TextChunk> newAreas = createChunksFromTag(root, t);
                    //System.out.println(root + " : " + t + " : " + newAreas);
                    for (TextChunk a : newAreas)
                    {
                        dest.add(a);
                    }
                }
            }
            else
            {
                //no tags, create untagged chunks
                List<TextChunk> newAreas = createUntaggedChunks(root);
                for (TextChunk a : newAreas)
                {
                    dest.add(a);
                }
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, dest);
        }
    }

    private List<TextChunk> createChunksFromTag(Area a, Tag t)
    {
        List<TextChunk> ret = new ArrayList<>();
        Tagger tg = tagConfig.getTaggerForTag(t);
        if (tg != null)
        {
            for (Box box : a.getBoxes())
            {
                String text = box.getOwnText();
                List<TagOccurrence> occurences = tg.extract(text);
                int last = 0;
                for (TagOccurrence occ : occurences)
                {
                    int pos = occ.getPosition();
                    if (pos > last) //some substring between, create a chunk with no tag
                    {
                        final TextChunk sepArea = createSubstringChunk(a, box, null, text.substring(last, pos), last);
                        ret.add(sepArea);
                    }
                    final TextChunk newArea = createSubstringChunk(a, box, t, occ.getText(), pos);
                    ret.add(newArea);
                    last = pos + occ.getLength();
                }
                if (text.length() > last)
                {
                    final TextChunk sepArea = createSubstringChunk(a, box, null, text.substring(last), last);
                    ret.add(sepArea);
                }
            }
        }
        else
            log.warn("Couldn't find tagger for {}", t);
        return ret;
    }

    private List<TextChunk> createUntaggedChunks(Area a)
    {
        List<TextChunk> ret = new ArrayList<>();
        for (Box box : a.getBoxes())
        {
            final String text = box.getOwnText();
            if (text != null && text.length() > 0)
            {
                final TextChunk sepArea = createSubstringChunk(a, box, null, text, 0);
                ret.add(sepArea);
            }
        }
        return ret;
    }
    
    private TextChunk createSubstringChunk(Area a, Box box, Tag tag, String occ, int pos)
    {
        Rectangular r = box.getSubstringBounds(pos, pos + occ.length());
        DefaultTextChunk newChunk = new DefaultTextChunk(r, a, box);
        newChunk.setId(idcnt++);
        newChunk.setText(occ);
        if (tag != null)
        {
            newChunk.setName("<chunk:" + tag.getName() + "> " + occ);
            newChunk.addTag(tag, a.getTagSupport(tag));
        }
        else
        {
            newChunk.setName("<---> " + occ);
        }
        return newChunk;
    }
    
}
