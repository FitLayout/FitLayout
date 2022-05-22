/**
 * MetadataChunksExtractor.java
 *
 * Created on 22. 5. 2022, 20:37:06 by burgetr
 */
package cz.vutbr.fit.layout.map.chunks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.StorageException;
import cz.vutbr.fit.layout.text.chunks.ChunksSource;

/**
 * 
 * @author burgetr
 */
public class MetadataChunksExtractor extends ChunksSource
{
    private RDFArtifactRepository repo;

    public MetadataChunksExtractor(Area root, RDFArtifactRepository repo)
    {
        super(root);
        this.repo = repo;
    }

    @Override
    public List<TextChunk> getTextChunks()
    {
        List<TextChunk> ret = new ArrayList<>();
        List<Example> examples = getExamples();
        for (Example ex : examples)
        {
            System.out.println(ex);
        }
        return ret;
    }
    
    private List<Example> getExamples()
    {
        try {
            final String query = "SELECT ?s ?p ?text WHERE {"
                    + "  ?s ?p ?text . "
                    + "  FILTER (isLiteral(?text)) "
                    + "}";
            
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
    
    private static class Example
    {
        private Resource subject;
        private IRI predicate;
        private String text;
        
        public Example(Resource subject, IRI predicate, String text)
        {
            this.subject = subject;
            this.predicate = predicate;
            this.text = text;
        }

        public Resource getSubject()
        {
            return subject;
        }

        public IRI getPredicate()
        {
            return predicate;
        }

        public String getText()
        {
            return text;
        }

        @Override
        public String toString()
        {
            return "Example [subject=" + subject + ", predicate=" + predicate
                    + ", text=" + text + "]";
        }
        
    }

}
