/**
 * DateTagger.java
 *
 * Created on 11.11.2011, 15:15:51 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.natty.DateGroup;
import org.natty.Parser;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;

/**
 * @author burgetr
 *
 */
public class DateTagger extends BaseTagger
{
    private static final float YES = 0.95f;
    private static final float NO = 0.0f;
    
    private static Map<String, String> dw;
    static {
        dw = new HashMap<String, String>();
        dw.put("jan", "january");
        dw.put("feb", "february");
        dw.put("mar", "march");
        dw.put("apr", "april");
        dw.put("may", "may");
        dw.put("jun", "june");
        dw.put("jul", "july");
        dw.put("aug", "august");
        dw.put("sep", "september");
        dw.put("oct", "october");
        dw.put("nov", "november");
        dw.put("dec", "december");
        dw.put("january", "january");
        dw.put("february", "february");
        dw.put("march", "march");
        dw.put("april", "april");
        dw.put("june", "june");
        dw.put("july", "july");
        dw.put("august", "august");
        dw.put("september", "september");
        dw.put("october", "october");
        dw.put("novebrer", "novebrer");
        dw.put("december", "december");
        
        dw.put("januar", "january");
        dw.put("februar", "february");
        dw.put("märz", "march");
        dw.put("april", "april");
        dw.put("mai", "may");
        dw.put("juni", "june");
        dw.put("juli", "july");
        dw.put("august", "august");
        dw.put("september", "september");
        dw.put("oktober", "october");
        dw.put("november", "november");
        dw.put("dezember", "december");
        
        dw.put("janvier", "january");
        dw.put("février", "february");
        dw.put("mars", "march");
        dw.put("avril", "april");
        dw.put("mai", "may");
        dw.put("juin", "june");
        dw.put("juillet", "july");
        dw.put("août", "august");
        dw.put("septembre", "september");
        dw.put("octobre", "october");
        dw.put("novembre", "november");
        dw.put("décembre", "december");
        
        dw.put("gennaio", "january");
        dw.put("febbraio", "february");
        dw.put("marzo", "march");
        dw.put("aprile", "april");
        dw.put("maggio", "may");
        dw.put("giugno", "june");
        dw.put("luglio", "july");
        dw.put("agosto", "august");
        dw.put("settembre", "september");
        dw.put("ottobre", "october");
        dw.put("novembre", "november");
        dw.put("dicembre", "december");
        
        dw.put("enero", "january");
        dw.put("febrero", "february");
        dw.put("marzo", "march");
        dw.put("abril", "april");
        dw.put("mayo", "may");
        dw.put("junio", "june");
        dw.put("julio", "july");
        dw.put("agosto", "august");
        dw.put("septiembre", "september");
        dw.put("octubre", "october");
        dw.put("noviembre", "november");
        dw.put("diciembre", "december");
    }
    
    protected Pattern[] dateexpr = {Pattern.compile("[1-2][0-9][0-9][0-9]\\-[0-9][0-9]\\-[0-9][0-9]")};
    
    private int dfirst;
    private int dlast;

    public DateTagger()
    {
    }
    
    @Override
    public String getId()
    {
        return "FITLayout.Tag.Date";
    }

    @Override
    public String getName()
    {
        return "Dates";
    }

    @Override
    public String getDescription()
    {
        return "Identifies dates in the most common formats";
    }
    
    @Override
    public float belongsTo(Area node)
    {
        if (node.isLeaf())
        {
            String text = node.getText().toLowerCase();
            //try to find some standard formats
            String[] words = text.split("\\s+");
            for (String s : words)
            {
                for (Pattern p : dateexpr)
                {
                    if (p.matcher(s).lookingAt()) 
                        return YES;
                }
            }
            //try to find a sequence of known words
            words = text.split("\\W+");
            return containsDate(words, 1) ? YES : NO;
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

    @Override
    public boolean mayCoexistWith(Tag other)
    {
        return true;
    }
    
    @Override
    public List<TagOccurrence> extract(String src)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        
        //check for common formats
        String[] words = src.toLowerCase().split("[^0-9\\-]");
        for (String s : words)
        {
            for (Pattern p : dateexpr)
            {
                Matcher match = p.matcher(s);
                if (match.lookingAt())
                {
                    ret.add(new TagOccurrence(match.group(), match.start(), YES));
                }
            }
        }
        //try to compose the individual values
        if (ret.isEmpty())
        {
            //words = src.toLowerCase().split("\\W+", 0);
            words = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS).split(src);
            if (findDate(words, 1))
            {
                String s = "";
                for (int i = dfirst; i <= dlast; i++)
                {
                    if (i != dfirst) s += " ";
                    s += words[i];
                }
                ret.add(new TagOccurrence(s, src.indexOf(words[dfirst]), YES));
            }
        }
        return ret;
    }
    
    public List<Date> extractDates(String s)
    {
        List<Date> ret = new ArrayList<Date>();
        
        List<TagOccurrence> srcdates = extract(s);
        for (TagOccurrence sdate : srcdates)
        {
            String[] words = sdate.getText().toLowerCase().split("\\s+");
            if (words.length == 1)
            {
                ret.add(strToDate(words[0]));
            }
            else
            {
                String[] sdates = new String[2];
                sdates[0] = "";
                sdates[1] = "";
                for (int round = 0; round < 2; round++)
                {
                    short prevtype = -1;
                    int order = 0;
                    String toadd = null;
                    for (int i = 0; i < words.length; i++)
                    {
                        short type = getValueType(words[i]);
                        if (prevtype != -1 && prevtype == type)
                            order++;
                        else
                        {
                            if (toadd != null)
                                sdates[round] += " " + toadd;
                            toadd = null;
                            order = 0;
                        }
                        if (order <= round)
                        {
                            toadd = words[i];
                            if (dw.containsKey(toadd))
                                toadd = dw.get(toadd);
                        }
                        prevtype = type;
                    }
                    if (toadd != null)
                        sdates[round] += " " + toadd;
                }
                
                for (int i = 0; i < 2; i++)
                {
                    if (!sdates[i].isEmpty())
                    {
                        Date d = strToDate(sdates[i]); 
                        if (d != null)
                            ret.add(d);
                    }
                }
            }
        }
        
        return ret;
    }
    
    private Date strToDate(String s)
    {
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(s);
        for(DateGroup group : groups) 
        {
          List<Date> dates = group.getDates();
          if (dates.size() > 0)
              return dates.get(0);
        }
        return null;
    }
    
    //=================================================================================================
    
    /**
     * Searches for sequences like num,month or month,num or num,year in the string.
     * @param strs list of words to be examined
     * @param tolerance maximal distance of the terms
     * @return <code>true</code> if the words form a date of some kind
     */
    private boolean containsDate(String[] strs, int tolerance)
    {
        dfirst = -1;
        dlast = -1;
        int lastdw = -999;
        int lastnum = -999;
        int lastyear = -999;
        for (int i = 0; i < strs.length; i++)
        {
            if (isMonthName(strs[i]))
            {
                lastdw = i;
                if (lastdw - lastnum <= tolerance)
                    return true;
            }
            else if (isYear(strs[i]))
            {
                lastyear = i;
                if (lastyear - lastnum <= tolerance)
                    return true;
            }
            else if (isNum(strs[i]))
            {
                lastnum = i;
                if (lastnum - lastdw <= tolerance)
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Searches for sequences like num,month or month,num or num,year in the string.
     * If the date is found, the dfirst and dlast properties are set to the indices of the first and last
     * index of the corresponding words. 
     * @param strs list of words to be examined
     * @param tolerance maximal distance of the terms
     * @return <code>true</code> if the words form a date of some kind
     */
    private boolean findDate(String[] strs, int tolerance)
    {
        dfirst = -1;
        dlast = -1;
        int curend = -1;
        int intpos = -1; //interesting position found (not a simple number)
        for (int i = 0; i < strs.length; i++)
        {
            if (isMonthName(strs[i]) || isYear(strs[i]))
                intpos = i;
            if (intpos == i || isNum(strs[i]))
            {
                if (isYear(strs[i]))
                    intpos = i;
                if (curend == -1)
                    curend = i;
                else
                {
                    if (i - curend <= tolerance) //extending the group
                        curend = i;
                    else //cannot extend the group
                    {
                        if (dlast - dfirst >= 1 && intpos >= dfirst && intpos <= dlast) //suitable group found
                            return true;
                        else //no suitable group, try the next one
                        {
                            curend = i;
                            dfirst = i;
                            dlast = i;
                        }
                                
                    }
                }
                if (dfirst == -1) dfirst = curend;
                dlast = curend;
            }
            
        }
        
        return (dlast - dfirst >= 1);
    }
    
    private boolean isNum(String s)
    {
        try
        {
            Integer.parseInt(stripSuffix(s));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    private boolean isYear(String s)
    {
        try
        {
            int n = Integer.parseInt(s);
            return n > 1900 && n < 2100;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isMonthNum(String s)
    {
        try
        {
            int n = Integer.parseInt(stripSuffix(s));
            return n >= 1 && n <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isMonthName(String s)
    {
        return dw.containsKey(s.toLowerCase());
    }
    
    private boolean isDayNum(String s)
    {
        try
        {
            int n = Integer.parseInt(stripSuffix(s));
            return n >= 1 && n <= 31;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private short getValueType(String s)
    {
        if (isMonthName(s))
            return 1;
        else if (isYear(s))
            return 2;
        else if (isMonthNum(s))
            return 3;
        else if (isDayNum(s))
            return 4;
        else if (isNum(s))
            return 5;
        else
            return 0;
    }
    
    private String stripSuffix(String s)
    {
        final String test = s.toLowerCase();
        if (test.endsWith("st") || test.endsWith("nd") || test.endsWith("rd") || test.endsWith("th"))
            return s.substring(0, s.length() - 2);
        else
            return s;
    }
}
