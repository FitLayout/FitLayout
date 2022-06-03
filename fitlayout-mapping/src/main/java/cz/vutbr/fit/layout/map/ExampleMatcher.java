/**
 * ExampleMatcher.java
 *
 * Created on 3. 6. 2022, 12:45:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

    public ExampleMatcher(MetadataExampleGenerator exampleGenerator)
    {
        this.exampleGenerator = exampleGenerator;
        examples = exampleGenerator.getStringExamples();
        floatExamples = exampleGenerator.getFloatExamples();
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


}
