package cz.vutbr.fit.layout.text.taggers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.text.tag.TextTag;


public class RegexpTagger extends BaseTagger
{
    private static final float YES = 0.6f;
    private static final float COULDBE = 0.1f;
    private static final float NO = 0.0f;
    
    /** Minimal number of words required in the title */
    private int minWords = 3;
    /** Minimal length of a word */
    private int minWordLength = 3;
    /** The expression the whole area must start with */
    private Pattern areaExpr = Pattern.compile("[A-Z0-9]"); //uppercase or number
    /** The expression describing the allowed title format */
    private Pattern mainExpr = Pattern.compile("[A-Z][A-Za-z\\s\\.\\:\\-\\p{Pd}]*");  //p{Pd} ~ Unicode Punctuation-dashes category
    /** The expression describing the allowed format of the title continuation */
    private Pattern contExpr = Pattern.compile("[A-Za-z\\s\\.\\:\\-\\p{Pd}]+"); 

    /** Words that are not allowed in the presentation title */
    protected List<String> blacklist;
    
    public RegexpTagger()
    {
        blacklist = new ArrayList<String>();
        //blacklist.add("session");
        //blacklist.add("chair");
    }
    
    @Override
    public String getId()
    {
        return "FITLayout.Tag.Regexp";
    }

    @Override
    public String getName()
    {
        return "Titles";
    }

    @Override
    public String getDescription()
    {
        return "General paper or news titles";
    }
    
    @Override
    public TextTag getTag()
    {
        return new TextTag("title", this);
    }

    //==========================================================================================
    
    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = List.of(
                new ParameterString("areaExpr", 0, 512),
                new ParameterString("mainExpr", 0, 512),
                new ParameterString("contExpr", 0, 512),
                new ParameterInt("minWords", 0, 100));
        return ret;
    }
    
    public String getAreaExpr()
    {
        return areaExpr.toString();
    }

    public void setAreaExpr(String areaExpr)
    {
        this.areaExpr = Pattern.compile(areaExpr);
    }

    public String getMainExpr()
    {
        return mainExpr.toString();
    }

    public void setMainExpr(String mainExpr)
    {
        this.mainExpr = Pattern.compile(mainExpr);
    }

    public String getContExpr()
    {
        return contExpr.toString();
    }

    public void setContExpr(String contExpr)
    {
        this.contExpr = Pattern.compile(contExpr);
    }

    public int getMinWords()
    {
        return minWords;
    }

    public void setMinWords(int minWords)
    {
        this.minWords = minWords;
    }

    //==========================================================================================
    
    @Override
    public float belongsTo(Area node)
    {
        if (node.isLeaf())
        {
            String text = getText(node);
            if (areaExpr.matcher(text).lookingAt()) //check the allowed text start
            {
                //check if there is a substring with the allowed format
                Matcher match = mainExpr.matcher(text);
                float ret = NO;
                while (match.find())
                {
                    String s = match.group();
                    String[] words = s.split("\\s+");
                    if (!containsBlacklistedWord(words))
                    {
                        if (wordCount(words) >= minWords) 
                            ret = YES;
                        else
                            ret = Math.max(ret, COULDBE);
                    }
                }
                return ret;
            }
        }
        return NO;
    }

    @Override
    public boolean allowsContinuation(Area node)
    {
        if (node.isLeaf())
        {
            String text = node.getText().trim();
            if (contExpr.matcher(text).lookingAt()) //must start with the allowed format
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allowsJoining()
    {
        return true;
    }
    
    @Override
    public boolean mayCoexistWith(Tag other)
    {
        return (!other.getValue().equals("session"));
    }
    
    @Override
    public List<TagOccurrence> extract(String src)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        
        Matcher match = mainExpr.matcher(src);
        while (match.find())
        {
            TagOccurrence occ = new TagOccurrence(match.group(), match.start(), COULDBE);
            String[] words = occ.getText().split("\\s+");
            if (wordCount(words) >= minWords)
                occ.setSupport(YES);
            ret.add(occ);
        }
        
        return ret;
    }
    
    //=================================================================================================
    
    protected String getText(Area node)
    {
        String s = node.getText().trim();
        //if (s.contains("\""))
        //    System.out.println("jo!");
        s = s.replaceAll("^[\\\"\\p{Pi}]+", "");
        s = s.replaceAll("[\\\"\\p{Pf}]+$", "");
        return s;
    }
    
    protected boolean containsBlacklistedWord(String[] words)
    {
        for (String w : words)
        {
            if (blacklist.contains(w.toLowerCase()))
                return true; 
        }
        return false;
    }
    
    protected int wordCount(String[] words)
    {
        int cnt = 0;
        for (String w : words)
        {
            if (w.length() >= minWordLength)
                cnt++;
        }
        return cnt;
    }
    
}
