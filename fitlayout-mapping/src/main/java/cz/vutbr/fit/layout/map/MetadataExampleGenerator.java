/**
 * MetadataExampleGenerator.java
 *
 * Created on 24. 5. 2022, 12:25:15 by burgetr
 */
package cz.vutbr.fit.layout.map;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

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
    
    public MetadataExampleGenerator(RDFArtifactRepository repo, IRI metadataContextIri)
    {
        this.repo = repo;
        this.contextIri = metadataContextIri;
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

}
