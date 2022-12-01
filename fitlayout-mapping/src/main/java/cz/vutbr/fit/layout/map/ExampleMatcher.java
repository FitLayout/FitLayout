/**
 * ExampleMatcher.java
 *
 * Created on 3. 6. 2022, 12:45:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.natty.DateGroup;
import org.natty.Parser;

import cz.vutbr.fit.layout.map.MetadataExampleGenerator.TempPrecision;

/**
 * Matches the generated examples as defined.
 * 
 * @author burgetr
 */
public class ExampleMatcher
{
    private MetadataExampleGenerator exampleGenerator;
   
    private Map<String, List<Example>> examples;
    private Map<Float, List<Example>> floatExamples;
    private Map<TemporalAccessor, List<Example>> dateExamples;
    private Map<TemporalAccessor, List<Example>> dateTimeMinutesExamples;
    private Map<TemporalAccessor, List<Example>> dateTimeSecondsExamples;

    public ExampleMatcher(MetadataExampleGenerator exampleGenerator)
    {
        this.exampleGenerator = exampleGenerator;
        examples = exampleGenerator.getStringExamples();
        floatExamples = exampleGenerator.getFloatExamples();
        dateExamples = exampleGenerator.getTemporalExamples(TempPrecision.DATES);
        dateTimeMinutesExamples = exampleGenerator.getTemporalExamples(TempPrecision.MINUTES);
        dateTimeSecondsExamples = exampleGenerator.getTemporalExamples(TempPrecision.SECONDS);
        System.out.println(dateExamples);
    }
    
    /**
     * Matches the configured examples to the given text and applies a consumer function
     * on the matched examples (if any).
     * 
     * @param rawText the raw text to match
     * @param op the consumer function to be applied on the matched examples
     * @return {@code true} if something has matched, {@code false} otherwise
     */
    public boolean match(String rawText, Consumer<List<Example>> op)
    {
        // no children matched, try this node
        Set<Example> usedExamples = new HashSet<>();
        boolean ret = tryStringExamples(rawText, usedExamples, op);
        if (!ret && !floatExamples.isEmpty())
            ret |= tryFloatExamples(rawText, usedExamples, op);
        if (!ret && !dateExamples.isEmpty())
            ret |= tryDateExamples(rawText, usedExamples, op);
        return ret;
    }
    
    private boolean tryStringExamples(String rawText, Set<Example> usedExamples, Consumer<List<Example>> op)
    {
        final String text = exampleGenerator.filterKey(rawText);
        final List<Example> mappedExamples = examples.get(text);
        
        if (mappedExamples != null && !mappedExamples.isEmpty())
        {
            //this.createChunksForArea(root, mappedExamples, dest);
            op.accept(mappedExamples);
            usedExamples.addAll(mappedExamples);
            return true;
        }
        else
            return false;
    }
    
    private boolean tryFloatExamples(String rawText, Set<Example> usedExamples, Consumer<List<Example>> op)
    {
        String text = rawText.replaceAll("[^a-zA-Z0-9\\,\\.]", "");
        if (text.contains(",") && !text.contains(".")) // may be comma used for decimals
            text = text.replace(',', '.');
        try {
            float val = Float.parseFloat(text);
            final List<Example> mappedExamples = floatExamples.get(val);
            if (mappedExamples != null)
            {
                mappedExamples.removeAll(usedExamples); //avoid re-using examples already mapped as strings
                if (!mappedExamples.isEmpty())
                {
                    //this.createChunksForArea(root, mappedExamples, dest);
                    op.accept(mappedExamples);
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }    

    private boolean tryDateExamples(String rawText, Set<Example> usedExamples, Consumer<List<Example>> op)
    {
        String text = rawText;
        if (text.contains("28 Nov"))
            System.out.println("jo!");
        try {
            TemporalAccessor ta = null;
            List<Example> mappedExamples = null;
            // try to convert the text to some temporal acessor and find mapped examples
            final Parser parser = new Parser();
            final List<DateGroup> groups = parser.parse(text);
            if (!groups.isEmpty())
            {
                final var group = groups.get(0);
                final var dates = group.getDates();
                if (!dates.isEmpty())
                {
                    final var date = dates.get(0);
                    final var instant = date.toInstant();
                    
                    if (group.isTimeInferred()) //only date is provided
                    {
                        ta = LocalDate.ofInstant(instant, MetadataExampleGenerator.TIME_ZONE);
                        mappedExamples = dateExamples.get(ta);
                    }
                    else //date time provided
                    {
                        ta = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        final int sec = instant.atZone(MetadataExampleGenerator.TIME_ZONE).getSecond();
                        if (sec == 0) //possibly truncated to minutes
                        {
                            mappedExamples = dateTimeMinutesExamples.get(ta);
                        }
                        else
                        {
                            mappedExamples = dateTimeSecondsExamples.get(ta);
                        }
                    }
                }
            }

            if (mappedExamples != null)
            {
                mappedExamples.removeAll(usedExamples); //avoid re-using examples already mapped as strings
                if (!mappedExamples.isEmpty())
                {
                    //this.createChunksForArea(root, mappedExamples, dest);
                    op.accept(mappedExamples);
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
            
        } catch (Exception e) {
            return false;
        }
    }    


}
