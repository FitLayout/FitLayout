/**
 * MetadataExampleGenerator.java
 *
 * Created on 24. 5. 2022, 12:25:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.unbescape.html.HtmlEscape;

import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;

/**
 * Implements extracting ocurrence examples from metadata.
 * 
 * @author burgetr
 */
public class MetadataExampleGenerator
{
    public static final ZoneId TIME_ZONE = ZoneId.systemDefault();
    
    public enum TempPrecision { DATES, MINUTES, SECONDS };
    
    private RDFArtifactRepository repo;
    private IRI contextIri;
    private Function<String, String> stringFilter;
    
    private List<Example> examples;
    
    
    /**
     * Creates an example generator for a metadata context in a repository with a identity
     * (useExact) key filter function.
     * @param repo
     * @param metadataContextIri
     */
    public MetadataExampleGenerator(RDFArtifactRepository repo, IRI metadataContextIri)
    {
        this.repo = repo;
        this.contextIri = metadataContextIri;
        this.stringFilter = MetadataExampleGenerator::useExact; 
    }

    /**
     * Creates an example generator for a metadata context in a repository with a configurable
     * key filter function.
     * @param repo
     * @param metadataContextIri
     * @param stringFilter the filter function to be applied to the strings in order to make them
     * a map key.
     */
    public MetadataExampleGenerator(RDFArtifactRepository repo, IRI metadataContextIri, Function<String, String> stringFilter)
    {
        this.repo = repo;
        this.contextIri = metadataContextIri;
        this.stringFilter = stringFilter; 
    }

    /**
     * Finds all literals in the given metadata context and creates a list of occurrence
     * examples containing the text, the corresponding property and subject.
     * @return A list of ocurrence examples.
     */
    public List<Example> getExamples()
    {
        if (examples == null)
            examples = readExamples();
        return examples;
    }
    
    protected List<Example> readExamples()
    {
        try {
            final String query = "SELECT ?s ?p ?text WHERE {"
                    + " GRAPH <" + contextIri + "> {"
                    + "  ?s ?p ?text . "
                    + "  FILTER (isLiteral(?text)) "
                    + "}}";
            
            List<BindingSet> data = repo.getStorage().executeSafeTupleQuery(query);
            List<Example> ret = new ArrayList<>(data.size());
            for (BindingSet binding : data)
            {
                Binding bS = binding.getBinding("s");
                Binding bP = binding.getBinding("p");
                Binding bText = binding.getBinding("text");
                if (bS != null && bP != null && bText != null 
                        && bS.getValue() instanceof Resource && bP.getValue() instanceof IRI)
                {
                    Example e = new Example((Resource) bS.getValue(), (IRI) bP.getValue(), bText.getValue().stringValue());
                    ret.add(e);
                }
            }
            return ret;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Creates the mapping from filtered strings to examples taken from the repository.
     * 
     * @return A map that maps filtered strings to the individual examples. Generally a list
     * of examples may be mapped to a single string.
     */
    public Map<String, List<Example>> getStringExamples()
    {
        var allExamples = getExamples();
        Map<String, List<Example>> ret = new HashMap<>();
        for (Example ex : allExamples)
        {
            final String text = stringFilter.apply(ex.getText());
            if (!text.isBlank())
            {
                List<Example> list = ret.get(text);
                if (list == null)
                {
                    list = new ArrayList<>(1);
                    ret.put(text, list);
                }
                list.add(ex);
            }
        }
        return ret;
    }

    /**
     * Creates the mapping from floats to examples for the examples that may be converted
     * to float.
     * 
     * @return A map that maps float values to the individual examples. Generally a list
     * of examples may be mapped to a single string.
     */
    public Map<Float, List<Example>> getFloatExamples()
    {
        var allExamples = getExamples();
        Map<Float, List<Example>> ret = new HashMap<>();
        for (Example ex : allExamples)
        {
            final Float key = getFloatValue(ex.getText());
            if (key != null)
            {
                List<Example> list = ret.get(key);
                if (list == null)
                {
                    list = new ArrayList<>(1);
                    ret.put(key, list);
                }
                list.add(ex);
            }
        }
        return ret;
    }

    /**
     * Creates the mapping from temporal types to examples for the examples that may be converted
     * to LocalDate or LocalDateTIme.
     * 
     * @return A map that maps LocalDate and LocalDateTime values to the individual examples. 
     * Generally a list of examples may be mapped to a single string.
     */
    public Map<TemporalAccessor, List<Example>> getTemporalExamples(TempPrecision precision)
    {
        var allExamples = getExamples();
        Map<TemporalAccessor, List<Example>> ret = new HashMap<>();
        for (Example ex : allExamples)
        {
            final TemporalAccessor key = getTemporalValue(ex.getText(), precision);
            if (key != null)
            {
                List<Example> list = ret.get(key);
                if (list == null)
                {
                    list = new ArrayList<>(1);
                    ret.put(key, list);
                }
                list.add(ex);
            }
        }
        return ret;
    }

    /**
     * Applies the configured keyFilter function to a source string.
     * 
     * @param src
     * @return
     */
    public String filterKey(final String src)
    {
        return stringFilter.apply(src);
    }
    
    /**
     * Converts a string to a float value if possible.
     * @param src
     * @return
     */
    public Float getFloatValue(String src)
    {
        if (src != null && !src.isBlank())
        {
            try {
                return Float.parseFloat(src);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else
            return null;
    }
    
    /**
     * Converts a string to a float value if possible.
     * @param src the source string value containing an ISO Date or DateTime
     * @param precision the required precision of the returned TemporalAccessor
     * @return the temporal acessor corresponding to the given value and the required precision
     */
    public TemporalAccessor getTemporalValue(String src, TempPrecision precision)
    {
        if (src != null && !src.isBlank())
        {
            TemporalAccessor ret = null;
            // try the schema:DateTime format of the value
            try {
                var ta = DateTimeFormatter.ISO_DATE_TIME.parse(src); 
                switch (precision) // choose the representation based on the required precision
                {
                    case DATES:
                        ret = LocalDate.ofInstant(Instant.from(ta), TIME_ZONE);
                        break;
                    case MINUTES:
                        ret = LocalDateTime.ofInstant(Instant.from(ta).truncatedTo(ChronoUnit.MINUTES), TIME_ZONE);
                        break;
                    case SECONDS:
                        ret = LocalDateTime.ofInstant(Instant.from(ta).truncatedTo(ChronoUnit.SECONDS), TIME_ZONE);
                        break;
                }
            } catch (Exception e) {
            }
            // if not succeeded, try the schema:Date format of the value
            if (ret == null)
            {
                try {
                    var ta = DateTimeFormatter.ISO_DATE.parse(src);
                    ret = LocalDate.ofInstant(Instant.from(ta), ZoneId.systemDefault()); // can only be represented as a date
                } catch (Exception e) {
                }
            }
            return ret;
        }
        else
            return null;
    }
    
    //=================================================================================================
    
    /**
     * A filter function that uses the string exactly as it is.
     * @param s the source string
     * @return s
     */
    public static String useExact(final String s)
    {
        return s;
    }
    
    /**
     * A filter function that normalizes the text which includes
     * <ul>
     * <li>Conversion to lower case</li>
     * <li>Reduction of sequences of white space characters to a single space</li>
     * <li>Replacing HTML entities by their values</li>
     * </ul>
     * @param src the source string
     * @return the normalized string
     */
    public static String normalizeText(final String src)
    {
        if (src == null)
            return "";
        else
        {
            final String text = src.toLowerCase().trim().replaceAll("\\s+", " "); //normalize white space
            return HtmlEscape.unescapeHtml(text); //remove HTML entities
        }
    }
    
}
