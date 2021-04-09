/**
 * HintSeparator.java
 *
 * Created on 29. 6. 2018, 15:38:44 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.text.tag.TagOccurrence;

/**
 * This hint tries to improve the recall of the tag occurrence discovery by applying
 * some regular separators.
 * 
 * @author burgetr
 */
public class HintSeparator extends DefaultHint
{
    //private static Logger log = LoggerFactory.getLogger(HintSeparator.class);
    
    private List<String> separators;
    private Tag tag;
    private Pattern pattern;

    
    public HintSeparator(Tag tag, List<String> separators, float support)
    {
        super("Separator", support);
        this.tag = tag;
        this.separators = separators;
        
        String ps = "";
        for (String sep : separators)
        {
            if (ps.length() > 0)
                ps += "|";
            ps += Pattern.quote(sep);
        }
        pattern = Pattern.compile(ps, Pattern.CASE_INSENSITIVE);
    }

    public Tag getTag()
    {
        return tag;
    }
    
    public List<String> getSeparators()
    {
        return separators;
    }
    
    @Override
    public String toString()
    {
        return "Separated by " + separators;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((separators == null) ? 0 : separators.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        HintSeparator other = (HintSeparator) obj;
        if (separators == null)
        {
            if (other.separators != null) return false;
        }
        else if (!separators.equals(other.separators)) return false;
        return true;
    }

    @Override
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences)
    {
        List<TagOccurrence> splitOccurrences = findOccurrencesBySeparators(boxText.toString());
        List<String> splits = new ArrayList<>(splitOccurrences.size());
        for (TagOccurrence occ : splitOccurrences)
            splits.add(occ.getText());
        List<String> occ = new ArrayList<>(occurrences.size());
        for (TagOccurrence o : occurrences)
            occ.add(o.getText());
        
        int io = 0;
        int is = 0;
        while (io < occ.size() && is < splits.size())
        {
            String cur = occ.get(io);
            if (!cur.equals(splits.get(is)))
            {
                if (is + 1 < splits.size() && cur.equals(splits.get(is + 1))) //found a single missing occurrence
                {
                    //log.debug("Found missing by separators: {}" , splits.get(is));
                    occurrences.add(splitOccurrences.get(is));
                    is++;
                }
                else //total mismatch, do nothing
                {
                    io++;
                    is++;
                }
            }
            else
            {
                io++;
                is++;
            }
        }
        return occurrences;
    }

    private List<TagOccurrence> findOccurrencesBySeparators(String text)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        Matcher match = pattern.matcher(text);
        int last = 0;
        while (match.find())
        {
            TagOccurrence occ = findOccurenceBetweeen(text, last, match.start() - 1);
            ret.add(occ);
            last = match.end();
        }
        if (last < text.length() - 1)
        {
            TagOccurrence occ = findOccurenceBetweeen(text, last, text.length() - 1);
            ret.add(occ);
        }
        return ret;
    }
    
    private TagOccurrence findOccurenceBetweeen(String text, int firstPos, int lastPos)
    {
        //trim whitespaces
        int begin = firstPos;
        while (begin < text.length() && Character.isWhitespace(text.charAt(begin)))
            begin++;
        int end = lastPos;
        while (end >= begin && Character.isWhitespace(text.charAt(end)))
            end--;
        //create occurrence
        return new TagOccurrence(text.substring(begin, end + 1), begin, 1.0f);
    }
    
}
