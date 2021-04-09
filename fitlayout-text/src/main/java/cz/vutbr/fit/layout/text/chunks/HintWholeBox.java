/**
 * HintWholeLine.java
 *
 * Created on 31. 10. 2018, 11:29:21 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.impl.DefaultContentLine;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.ContentLine;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.text.tag.TagOccurrence;

/**
 * A hint that forces using the whole source box for the corresponding chunk even if only part
 * of the box was detected as the chunk. 
 * @author burgetr
 */
public class HintWholeBox extends DefaultHint
{
    
    public HintWholeBox(Tag tag, float support)
    {
        super("WholeBox", support);
        setBlock(true);
    }

    @Override
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences)
    {
        if (occurrences.isEmpty())
            return occurrences; //no occurences - do nothing
        else
        {
            List<TagOccurrence> ret = new ArrayList<>();
            TagOccurrence occ = new TagOccurrence(boxText.getText(), 0, 1.0f);
            ret.add(occ);
            return ret;
        }
    }

    @Override
    public List<TextChunk> processChunks(Area src, List<TextChunk> areas)
    {
        //put all the resulting areas to a common logical content line
        ContentLine line = new DefaultContentLine(areas.size());
        line.addAll(areas);
        return areas;
    }

}
