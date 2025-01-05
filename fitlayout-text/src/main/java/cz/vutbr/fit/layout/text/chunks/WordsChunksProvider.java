/**
 * WordsChunksProvider.java
 *
 * Created on 5. 1. 2025, 14:51:19 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultChunkSet;
import cz.vutbr.fit.layout.impl.DefaultTextChunk;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.ChunkSet;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * Creates text chunks from individual words in the leaf areas.
 * 
 * @author burgetr
 */
public class WordsChunksProvider extends BaseArtifactService
{
    private boolean propagateTags;
    
    private int idCounter = 1;

    public WordsChunksProvider()
    {
        propagateTags = true;
    }
    
    public boolean getPropagateTags()
    {
        return propagateTags;
    }

    public void setPropagateTags(boolean propagateTags)
    {
        this.propagateTags = propagateTags;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.WordsChunks";
    }
    
    @Override
    public String getName()
    {
        return "Break leaf areas to single words";
    }

    @Override
    public String getDescription()
    {
        return "Creates text chunks from individual words in the leaf areas.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return List.of(new ParameterBoolean("propagateTags", "Move eventual tags to sub-areas"));
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return SEGM.ChunkSet;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof AreaTree)
            return extractChunks((AreaTree) input);
        else
            throw new ServiceException("Source artifact not specified or not an area tree");
    }
    
    //==================================================================================
    
    private ChunkSet extractChunks(AreaTree atree)
    {
        List<TextChunk> chunks = new ArrayList<>();
        recursiveBreakAreas(atree.getRoot(), chunks);
        DefaultChunkSet ret = new DefaultChunkSet(atree.getIri(), new HashSet<>(chunks));
        ret.setPageIri(atree.getPageIri());
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        return ret;
    }
    
    /**
     * Goes through all the areas in the tree and tries to join their sub-areas into single
     * areas.
     */
    protected void recursiveBreakAreas(Area root, List<TextChunk> dest)
    {
        if (root.isLeaf())
        {
            breakIntoWords(root, dest);
        }
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
                recursiveBreakAreas(root.getChildAt(i), dest);
        }
    }
    
    private void breakIntoWords(Area a, List<TextChunk> dest)
    {
        String text = a.getText();
        if (text != null && !text.isBlank())
        {
            Rectangular origBounds = a.getBounds();
            final float charWidth = (float) origBounds.getWidth() / text.length(); // average width of a character
            // split the text into words together with the bounds
            List<String> words = new ArrayList<>();
            List<Rectangular> wordBounds = new ArrayList<>();
            int wordStart = -1;
            for (int i = 0; i < text.length(); i++)
            {
                char c = text.charAt(i);
                if (Character.isWhitespace(c))
                {
                    if (wordStart != -1)
                    {
                        words.add(text.substring(wordStart, i));
                        final int wordX = Math.round(origBounds.getX1() + wordStart * charWidth);
                        final int wordWidth = Math.round((i - wordStart) * charWidth);
                        wordBounds.add(new Rectangular(wordX, origBounds.getY1(), wordX + wordWidth - 1, origBounds.getY2()));
                        wordStart = -1;
                    }
                }
                else
                {
                    if (wordStart == -1)
                        wordStart = i;
                }
            }
            if (wordStart!= -1)
            {
                words.add(text.substring(wordStart));
                final int wordX = Math.round(origBounds.getX1() + wordStart * charWidth);
                final int wordWidth = Math.round((text.length() - wordStart) * charWidth);
                wordBounds.add(new Rectangular(wordX, origBounds.getY1(), wordX + wordWidth - 1, origBounds.getY2()));
            }
            // create chunks
            for (int i = 0; i < words.size(); i++)
                dest.add(createChunk(a, words.get(i), wordBounds.get(i)));
        }
    }

    private TextChunk createChunk(Area area, String word, Rectangular bounds)
    {
        var chunk = new DefaultTextChunk();
        chunk.setId(this.idCounter++);
        chunk.setBounds(bounds);
        chunk.setSourceArea(area);
        chunk.setText(word);
        copyTags(area, chunk);
        if (propagateTags)
        {
            Area ancestor = area.getParent();
            while (ancestor != null)
            {
                copyTags(ancestor, chunk);
                ancestor = ancestor.getParent();
            }
        }
        chunk.setName("<chunk:" + getTagNames(chunk) + "> " + word);
        return chunk;
    }
    
    private void copyTags(Area src, DefaultTextChunk dest)
    {
        for (var entry : src.getTags().entrySet())
        {
            dest.addTag(entry.getKey(), entry.getValue());
        }
    }
    
    private String getTagNames(TextChunk chunk)
    {
        StringBuilder sb = new StringBuilder();
        for (var tag : chunk.getTags().keySet())
            sb.append(tag.getName()).append(" ");
        return sb.toString().trim();
    }

}
