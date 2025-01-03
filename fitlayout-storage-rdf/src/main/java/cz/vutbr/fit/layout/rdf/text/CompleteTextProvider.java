/**
 * CompleteTextProvider.java
 *
 * Created on 4. 12. 2023, 21:12:00 by burgetr
 */
package cz.vutbr.fit.layout.rdf.text;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.util.Values;

import cz.vutbr.fit.layout.api.AreaConcatenator;
import cz.vutbr.fit.layout.api.Concatenators;
import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.FL;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;

/**
 * A service that adds complete text to every area in the RDF area tree.
 * 
 * @author burgetr
 */
public class CompleteTextProvider extends BaseArtifactService
{
    private boolean leavesOnly;
    private String separator;

    public CompleteTextProvider()
    {
        this.leavesOnly = false;
        this.separator = "%";
    }
    
    public CompleteTextProvider(boolean leavesOnly, String separator)
    {
        this.leavesOnly = leavesOnly;
        this.separator = separator;
    }
    
    public boolean getLeavesOnly()
    {
        return leavesOnly;
    }

    public void setLeavesOnly(boolean leavesOnly)
    {
        this.leavesOnly = leavesOnly;
    }

    public String getSeparator()
    {
        return separator;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }

    @Override
    public String getId()
    {
        return "FitLayout.CompleteText";
    }

    @Override
    public String getName()
    {
        return "Add Complete Text to Areas";
    }

    @Override
    public String getDescription()
    {
        return "Adds the a:textContent property to every area in the RDF area tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return List.of(
                new ParameterBoolean("leavesOnly", "Include only leaf areas"),
                new ParameterString("separator", "Separator for text content (use % for space)", 0, 6));
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return null;
    }

    @Override
    public String getCategory()
    {
        return "Text";
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof RDFAreaTree)
        {
            var repo = getServiceManager().getArtifactRepository();
            if (repo instanceof RDFArtifactRepository)
            {
                // the area concatenator for applying the separator
                final String esep = separator.replace("%", " ");
                final AreaConcatenator concat = new Concatenators.SeparatedAreaConcatenator(esep, Concatenators.getDefaultBoxConcatenator());
                // create a new model with the text content triples
                Model model = (new DynamicModelFactory()).createEmptyModel();
                recursiveAddText(((AreaTree) input).getRoot(), model, concat);
                // insert the processedBy statement
                model.add(input.getIri(), FL.processedBy, Values.literal(getId()));
                // store the model in the context
                var storage = ((RDFArtifactRepository) repo).getStorage();
                storage.insertGraph(model, input.getIri());
                return null;
            }
            else
                throw new ServiceException("RDFArtifactRepository is required for storing the connections");
        }
        else
            throw new ServiceException("Source artifact not specified or not an RDF area tree");    
    }
    
    private void recursiveAddText(Area root, Model model, AreaConcatenator concat)
    {
        if (!leavesOnly || root.isLeaf())
        {
            IRI subj = ((RDFArea) root).getIri();
            String text = root.getText(concat);
            model.add(subj, SEGM.text, Values.literal(text));
        }
        for (Area child : root.getChildren())
            recursiveAddText(child, model, concat);
    }
    
}
