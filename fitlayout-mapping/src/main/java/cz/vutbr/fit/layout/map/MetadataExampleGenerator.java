/**
 * MetadataExampleGenerator.java
 *
 * Created on 24. 5. 2022, 12:25:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

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
    private RDFArtifactRepository repo;
    private IRI contextIri;
    private Function<String, String> keyFilter;
    
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
        this.keyFilter = MetadataExampleGenerator::useExact; 
    }

    /**
     * Creates an example generator for a metadata context in a repository with a configurable
     * key filter function.
     * @param repo
     * @param metadataContextIri
     * @param textFilter the filter function to be applied to the strings in order to make them
     * a map key.
     */
    public MetadataExampleGenerator(RDFArtifactRepository repo, IRI metadataContextIri, Function<String, String> keyFilter)
    {
        this.repo = repo;
        this.contextIri = metadataContextIri;
        this.keyFilter = keyFilter; 
    }

    /**
     * Finds all literals in the given metadata context and creates a list of occurrence
     * examples containing the text, the corresponding property and subject.
     * @return A list of ocurrence examples.
     */
    public List<Example> getExamples()
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
    public Map<String, List<Example>> getMappedExamples()
    {
        var allExamples = getExamples();
        Map<String, List<Example>> ret = new HashMap<>();
        for (Example ex : allExamples)
        {
            final String text = keyFilter.apply(ex.getText());
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
     * Applies the configured keyFilter function to a source string.
     * 
     * @param src
     * @return
     */
    public String filterKey(final String src)
    {
        return keyFilter.apply(src);
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
