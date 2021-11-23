/**
 * RDFPage.java
 *
 * Created on 13. 1. 2016, 22:33:22 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.impl.DefaultPage;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class RDFPage extends DefaultPage implements RDFResource, RDFArtifact
{
    protected Map<IRI, RDFBox> boxIris;
    private Set<Statement> additionalStatements;
    

    public RDFPage(URL url)
    {
        super(url);
    }

    public RDFPage(Page src, IRI pageIri)
    {
        super(src);
        setIri(pageIri);
    }

    public Map<IRI, RDFBox> getBoxIris()
    {
        return boxIris;
    }

    public void setBoxIris(Map<IRI, RDFBox> boxUris)
    {
        this.boxIris = boxUris;
    }

    public RDFBox findBoxByIri(IRI uri)
    {
        if (boxIris != null)
            return boxIris.get(uri);
        else
            return null;
    }

    public void setAdditionalStatements(Set<Statement> additionalStatements)
    {
        this.additionalStatements = additionalStatements;
    }

    @Override
    public Set<Statement> getAdditionalStatements()
    {
        return additionalStatements;
    }

    @Override
    public void recompute()
    {
        if (getRoot() != null)
            recursiveInvalidateStyle(getRoot());
    }
    
    private void recursiveInvalidateStyle(Box root)
    {
        root.childrenChanged();
        for (Box child : root.getChildren())
            recursiveInvalidateStyle(child);
    }

}
