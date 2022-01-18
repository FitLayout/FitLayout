/**
 * PresentationBasedChunksSource.java
 *
 * Created on 29. 6. 2018, 15:16:19 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.text.tag.TextTag;

/**
 * A chunk source that follows some presentation patterns in order to improve the chunk extraction.
 * 
 * Chunk extraction goes through the tree of areas. For every leaf area of the tree, the chunk extraction
 * consists of the following phases:
 * 
 * <ol>
 * <li>Box extraction - extraction of a source boxes from the given source areas. We obtain a list of boxes that
 * are later used as the source text for extracting the text chunks.
 * <li>Occurence extraction - location of the tag occurences in the source box text. We obtain a list of occurences.
 * <li>Chunk creation - creation of the chunks from the occurences. We obtain a list of chunks found in the box text.
 * </ol>
 * 
 * Finally, the chunks obtained from the individual areas are joined to a single list. The individual phases
 * of the extraction may be influenced by different presentation hints registered using
 * the {@link #addHint(Tag, PresentationHint)} method.
 * 
 * @author burgetr
 */
public class PresentationBasedChunksSource extends ChunksSource
{
    private static Logger log = LoggerFactory.getLogger(PresentationBasedChunksSource.class);
    
    private int idcnt;
    private float minTagSupport;
    private ChunksCache cache;
    private List<TextChunk> areas;
    private Map<Tag, List<TextChunk>> tagAreas;
    private Map<Tag, List<PresentationHint>> hints;
    
    /**
     * Creates a new source.
     * @param root the root area of the area tree
     * @param minTagSupport minimal support of the tags for considering the areas for chunk extraction
     * @param cache the cache of already extracted chunks for sharing the chunks among different sources
     * or {@code null} when no cache should be used.
     */
    public PresentationBasedChunksSource(Area root, float minTagSupport, ChunksCache cache)
    {
        super(root);
        this.minTagSupport = minTagSupport;
        this.cache = cache;
        hints = new HashMap<>();
        idcnt = 1;
    }
    
    @Override
    public List<TextChunk> getTextChunks()
    {
        if (areas == null)
        {
            tagAreas = new HashMap<>();
            Set<TextTag> supportedTags = findLeafTags(getRoot());
            if (!supportedTags.isEmpty())
            {
                for (TextTag t : supportedTags)
                {
                    final List<PresentationHint> hintList = hints.get(t);
                    List<TextChunk> chunks = null;
                    if (cache != null)
                        chunks = cache.get(t, hintList);
                    if (chunks == null)
                    {
                        chunks = extractChunks(t, hintList);
                        if (cache != null)
                            cache.put(t, hintList, chunks);
                    }
                    tagAreas.put(t, chunks);
                }
            }
            areas = disambiguateAreas(tagAreas);
        }
        return areas;
    }

    public void addHint(Tag tag, PresentationHint hint)
    {
        List<PresentationHint> list = hints.get(tag);
        if (list == null)
        {
            list = new ArrayList<>();
            hints.put(tag, list);
        }
        list.add(hint);
    }

    @Override
    public String toString()
    {
        return "ChunkSource" + hints.toString();
    }
    
    //==============================================================================================
    
    private List<TextChunk> extractChunks(Tag t, List<PresentationHint> hints)
    {
        List<TextChunk> destChunks = new ArrayList<>();
        List<TextChunk> destAll = new ArrayList<>();
        Set<Area> processed = new HashSet<>();
        recursiveScan(getRoot(), (TextTag) t, destChunks, destAll, processed);
        //apply post-processing hints on all chunks for the given tag
        if (hints != null)
            destChunks = applyHints(destChunks, hints);
        //create a layer topology for all the created areas
        final List<ContentRect> rects = new ArrayList<>(destAll.size());
        rects.addAll(destAll);
        
        return destChunks;
    }
    
    private List<TextChunk> disambiguateAreas(Map<Tag, List<TextChunk>> areas)
    {
        //TODO implement merging the individual lists to a single list of tagged areas
        List<TextChunk> all = new ArrayList<>();
        for (List<TextChunk> sub : areas.values())
            all.addAll(sub);
        
        /*AreaTopology t = new AreaListGridTopology(all);
        for (Area a : all)
        {
            Rectangular gp = t.getPosition(a);
            Collection<Area> isec = t.findAllAreasIntersecting(gp);
            for (Area other : isec)
            {
                if (other != a)
                {
                    System.out.println(a + " intersects with " + other);
                }
            }
        }*/
        
        return all;
    }
    
    private List<TextChunk> applyHints(List<TextChunk> areas, List<PresentationHint> hints)
    {
        List<TextChunk> current = areas;
        for (PresentationHint hint : hints)
            current = hint.postprocessChunks(current);
        return current;
    }
    
    //==============================================================================================
    
    private void recursiveScan(Area root, TextTag tag, List<TextChunk> destChunks, List<TextChunk> destAll, Set<Area> processed)
    {
        if (root.isLeaf())
        {
            if (root.hasTag(tag, minTagSupport) && !processed.contains(root))
            {
                createAreasFromTag(root, tag, destChunks, destAll, processed);
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveScan(child, tag, destChunks, destAll, processed);
        }
    }

    private void createAreasFromTag(Area a, TextTag t, List<TextChunk> destChunks, List<TextChunk> destAll, Set<Area> processed)
    {
        List<TextChunk> chunks = new ArrayList<>();
        List<TextChunk> all = new ArrayList<>();
        Tagger tg = t.getSource();
        
        //Stage 1: Extract boxes
        SourceBoxList boxes = extractBoxes(a, t, processed);
        BoxText boxText = new BoxText(boxes);

        //Stage 2: Find occurences
        List<TagOccurrence> occurrences = tg.extract(boxText.getText());
        //apply hints on the particular list of occurences
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                occurrences = hint.processOccurrences(boxText, occurrences);
        }
        
        //Stage 3: Create chunks based on the occurences
        int last = 0;
        for (TagOccurrence occ : occurrences)
        {
            if (occ.getLength() > 0)
            {
                int pos = occ.getPosition();
                if (pos > last) //some substring between, create a chunk with no tag
                {
                    String substr = boxText.getText().substring(last, pos);
                    TagOccurrence between = new TagOccurrence(substr, last, 1);
                    TextChunk sepArea = createSubstringArea(a, t, false, boxText, between);
                    all.add(sepArea);
                }
                TextChunk newArea = createSubstringArea(a, t, true, boxText, occ);
                chunks.add(newArea);
                all.add(newArea);
                last = pos + occ.getLength();
            }
            else
            {
                log.error("Zero length occurence: {}, tag {}, area {}", occ, t, a);
            }
        }
        if (boxText.length() > last) //there is something remaining after the last occurrence
        {
            String substr = boxText.getText().substring(last);
            TagOccurrence between = new TagOccurrence(substr, last, 1);
            TextChunk sepArea = createSubstringArea(a, t, false, boxText, between);
            all.add(sepArea);
        }
        //apply hints on the particular list of chunks
        List<TextChunk> current = chunks;
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                current = hint.processChunks(a, current);
        }
        destChunks.addAll(chunks);
        destAll.addAll(all);
    }

    private TextChunk createSubstringArea(Area a, TextTag tag, boolean present, BoxText boxText, TagOccurrence occ)
    {
        //determine the substring bounds
        Rectangular r = boxText.getSubstringBounds(occ.getPosition(), occ.getPosition() + occ.getLength());
        //create the chunk area
        DefaultTextChunk newChunk = new DefaultTextChunk(r, a, boxText.getBoxForPosition(occ.getPosition()));
        newChunk.setId(idcnt++);
        newChunk.setText(occ.getText());
        if (present)
        {
            newChunk.setName("<chunk:" + tag.getName() + "> " + occ);
            newChunk.addTag(tag, a.getTagSupport(tag));
        }
        else
        {
            newChunk.setName("<chunk:!" + tag.getName() + "> " + occ);
        }
        return newChunk;
    }
    
    //==============================================================================================
    
    private SourceBoxList extractBoxes(Area src, Tag t, Set<Area> processed)
    {
        processed.add(src);
        SourceBoxList current = new SourceBoxList(src.getBoxes(), true); //we start with block layout (single line)
        if (hints.containsKey(t))
        {
            for (PresentationHint hint : hints.get(t))
                current = hint.extractBoxes(src, current, processed);
        }
        return current;
    }
    
    //==============================================================================================
    
    private Set<TextTag> findLeafTags(Area root)
    {
        Set<TextTag> ret = new HashSet<>();
        recursiveCollectTags(root, ret);
        return ret;
    }

    private void recursiveCollectTags(Area root, Set<TextTag> dest)
    {
        if (root.isLeaf())
        {
            Set<Tag> all = root.getSupportedTags(minTagSupport);
            for (Tag t : all)
            {
                if (t instanceof TextTag)
                    dest.add((TextTag) t);
            }
        }
        else
        {
            for (Area child : root.getChildren())
                recursiveCollectTags(child, dest);
        }
    }
    
}
