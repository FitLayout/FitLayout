/**
 * NumberTagger.java
 *
 * Created on 9. 2. 2016, 9:38:26 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;


/**
 * 
 * @author burgetr
 */
public abstract class IntegerTagger extends BaseTagger
{
    private static final float YES = 0.9f;
    private static final float NO = 0.0f;
    
    private int min;
    private int max;
    private boolean allowsLeadingZero = false;

    private Pattern numexpr;
    
    
    public IntegerTagger(int min, int max)
    {
        super();
        this.min = min;
        this.max = max;
    }
    
    public int getMin()
    {
        return min;
    }

    public int getMax()
    {
        return max;
    }

    public boolean isAllowsLeadingZero()
    {
        return allowsLeadingZero;
    }

    public void setAllowsLeadingZero(boolean allowsLeadingZero)
    {
        this.allowsLeadingZero = allowsLeadingZero;
    }

    @Override
    public String getDescription()
    {
        return "Numbers from " + min + " to " + max;
    }
    
    @Override
    public float belongsTo(Area node)
    {
        if (node.isLeaf())
        {
            String text = node.getText();
            Matcher match = getNumExpr().matcher(text);
            while (match.find())
            {
                if (validateMatch(text, match.group(), match.start(), match.end()))
                    return YES;
            }
        }
        return NO;
    }
    
    @Override
    public boolean allowsContinuation(Area node)
    {
        return false;
    }

    @Override
    public boolean allowsJoining()
    {
        return false;
    }
    
    public boolean mayCoexistWith(Tag other)
    {
        return true;
    }
    
    @Override
    public List<TagOccurrence> extract(String src)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        
        Matcher match = getNumExpr().matcher(src);
        while (match.find())
        {
            if (validateMatch(src, match.group(), match.start(), match.end()))
                ret.add(new TagOccurrence(match.group(), match.start(), YES));
        }
        
        return ret;
    }

    /**
     * Validates the substring match -- checks the neighborhood and numeric range. This may be overriden
     * for particular use cases.
     * @param srcString the whole source string
     * @param substring the matched substring
     * @param matchStart start index of the substring in the whole string
     * @param matchEnd end index of the substring in the whole string
     * @return true when the match corresponds to the number range and other limitations
     */
    protected boolean validateMatch(String srcString, String substring, int matchStart, int matchEnd)
    {
        if ((matchStart == 0 || !Character.isAlphabetic(srcString.codePointAt(matchStart))) && //require something non-alphabetic chars around
                (matchEnd == srcString.length() || !Character.isAlphabetic(srcString.codePointAt(matchEnd))))
        {
            final int num = Integer.parseInt(substring);
            return validateRange(num);
        }
        return false;
    }
    
    protected boolean validateRange(int value)
    {
        return (value >= getMin() && value <= getMax());
    }
    
    protected Pattern getNumExpr()
    {
        if (numexpr == null)
        {
            final String lead = isAllowsLeadingZero() ? "[0-9]" : "[1-9]";
            final String re = lead + "[0-9]*";
            numexpr = Pattern.compile(re);
        }
        return numexpr;
    }
    
}
