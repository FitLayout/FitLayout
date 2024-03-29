/**
 * HintMultiBox.java
 *
 * Created on 3. 1. 2019, 13:05:43 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.api.AreaUtils;
import cz.vutbr.fit.layout.api.TagResolver;
import cz.vutbr.fit.layout.impl.DefaultContentLine;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.ContentLine;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * This hint has the same meaning as {@link HintWholeBox} but it also considers multiple subsequent boxes. 
 * @author burgetr
 */
public class HintMultiBox extends DefaultHint
{
    private static final int STEPDIF_THRESHOLD = 2; //pixels tolerance in the step difference between lines

    private Tag tag;
    private TagResolver dis;

    
    public HintMultiBox(Tag tag, TagResolver dis, float support)
    {
        super("MultiBox", support);
        setBlock(true);
        this.tag = tag;
        this.dis = dis;
    }

    @Override
    public SourceBoxList extractBoxes(Area a, SourceBoxList current, Set<Area> processed)
    {
        final Set<Tag> dtags = dis.getAreaTags(a);
        if (dtags.contains(tag)) //the resolver must assign the destination tag to this area
        {
            if (a.getParent() != null) //the area must have a parent in order to work with the topology
            {
                //try to discover subsequent lines
                AreaTopology topology = a.getParent().getTopology();
                List<Area> lines = findConsistentLines(a, topology);
                if (lines.size() > 1) //more than the source line found
                {
                    //System.out.println("Consistent lines for A=" + a);
                    for (Area aa : lines)
                    {
                        //System.out.println("    " + aa);
                        if (aa != a)
                        {
                            current.addAll(aa.getBoxes());
                            processed.add(aa);
                        }
                    }
                }
                return current;
            }
            else
                return current; //no operation
        }
        else
            return current;
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
    
    //===================================================================================================================
    
    private List<Area> findConsistentLines(Area a, AreaTopology topology)
    {
        List<Area> ret = new ArrayList<>();
        ret.add(a);
        Area last = a;
        Rectangular lastr = topology.getPosition(last);
        int laststep = -1;
        boolean found = true;
        while (found)
        {
            found = false;
            Area next = findLineBelow(last, topology);
            if (next != null)
            {
                Rectangular nextr = topology.getPosition(last);
                
                int step = nextr.getY1() - lastr.getY2();
                int stepdif = Math.abs(laststep - step);
                
                final Set<Tag> dtags = dis.getAreaTags(next);
                if (dtags.contains(tag) //inferred tags correspond to the target tag
                        && (laststep == -1 || stepdif <= STEPDIF_THRESHOLD))
                {
                    ret.add(next);
                    last = next;
                    lastr = nextr;
                    laststep = step;
                    found = true;
                }
            }
        }
        return ret;
    }
    
    private Area findLineBelow(Area a, AreaTopology topology)
    {
        List<ContentRect> cands = new ArrayList<>(); 
        AreaUtils.findAreasBelow(a, topology, cands);
        if (cands.size() == 1)
            return (Area) cands.get(0);
        else
            return null;
    }
    
}
