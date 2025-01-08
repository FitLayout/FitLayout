/**
 * TextChunkDOMLinkProvider.java
 *
 * Created on 7. 1. 2025, 18:03:47 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Creates connections between text chunks that are related to the same DOM elements
 * of selected types.
 * 
 * @author burgetr
 */
public class TextChunkDOMLinkProvider extends ConnectionSetArtifactService
{
    private static final Set<String> tagNames = Set.of("a", "p",
            "h1", "h2", "h3", "h4", "h5", "h6", "td");
    
    public TextChunkDOMLinkProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.TextChunkDOMLinks";
    }

    @Override
    public String getName()
    {
        return "Text chunk DOM-based links extractor";
    }

    @Override
    public String getDescription()
    {
        return "Creates connections between text chunks that are related to the same DOM elements of selected types.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return Collections.emptyList();
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.ChunkSet;
    }

    @Override
    public IRI getProduces()
    {
        return null;
    }

    @Override
    public String getCategory()
    {
        return "Relations";
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof ChunkSet)
        {
            ChunkSet cset = (ChunkSet) input;
            final var conns = extractConnections(cset);
            saveConnections(input.getIri(), conns);
            return null; // no new artifact created
        }
        else
            throw new ServiceException("Source artifact not specified or not a chunk set");
    }
    
    //================================================================================
    
    public Collection<AreaConnection> extractConnections(ChunkSet cset)
    {
        Map<String, List<TextChunk>> index = buildParentIndex(cset.getTextChunks());
        List<AreaConnection> connections = new LinkedList<>();
        for (var entry : index.entrySet())
        {
            Collections.sort(entry.getValue(), (c1, c2) -> compareChunks(c1, c2));
            TextChunk prev = null;
            for (var chunk : entry.getValue())
            {
                if (prev != null)
                    connections.add(new AreaConnection(prev, chunk, Relations.HASCONT, 1.0f));
                prev = chunk;
            }
        }
        return connections;
    }
    
    public Map<String, List<TextChunk>> buildParentIndex(Collection<TextChunk> chunks)
    {
        Map<String, List<TextChunk>> ret = new HashMap<>();
        for (TextChunk chunk : chunks)
        {
            var parentId = getDOMParentId(chunk);
            if (parentId != null)
                ret.computeIfAbsent(parentId, k -> new LinkedList<>()).add(chunk);
        }
        return ret;
    }
    
    private int compareChunks(TextChunk c1, TextChunk c2)
    {
        // Sort by the X a Y coordinates
        final Rectangular r1 = c1.getBounds();
        final Rectangular r2 = c2.getBounds();
        int diffY = Integer.compare(r1.getY1(), r2.getY1());
        if (diffY != 0)
            return diffY;
        else
            return Integer.compare(r1.getX1(), r2.getX1());
    }
    
    private String getDOMParentId(TextChunk chunk)
    {
        var area = chunk.getSourceArea();
        if (area != null)
        {
            var cur = area;
            var tag = getTagName(cur);
            while (cur != null && !tagNames.contains(tag))
            {
                cur = cur.getParent();
                tag = getTagName(cur);
            }
            
            if (cur != null)
                return getDOMParentId(cur);
            else
                return null;
        }
        else
        {
            return null;
        }
    }
    
    private String getTagName(Area area)
    {
        if (area != null && area.getBoxes().size() == 1)
        {
            // The areas should contain only one box with the tag name.
            var tag = area.getBoxes().get(0).getTagName();
            if (tag != null)
                return tag.toLowerCase();
            else
                return "";
        }
        else
            return "";
    }

    private String getDOMParentId(Area area)
    {
        final List<Box> boxes = area.getBoxes();
        if (!boxes.isEmpty())
            return boxes.get(0).getSourceNodeId();
        else
            return null;
    }

}
