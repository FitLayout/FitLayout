/**
 * ModelTransformer.java
 *
 * Created on 30. 3. 2021, 18:38:17 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFBox;

/**
 * A base class for all model creators and loaders.
 * 
 * @author burgetr
 */
public class ModelTransformer
{
    private IRIFactory iriFactory;
    
    
    public ModelTransformer(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }

    /**
     * Gets the IRI factory used for creating the IRIs when building a RDF graph.
     * @param iriFactory
     */
    public IRIFactory getIriFactory()
    {
        return iriFactory;
    }

    /**
     * Configures the IRI factory used for creating the IRIs when building a RDF graph.
     * @param iriFactory
     */
    public void setIriFactory(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }
    
    /**
     * Reuses a box IRI or creates a new one if the box is not an RDFBox.
     * @param pageIri the IRI of the page the box belongs to
     * @param box the box itself
     * @return the IRI
     */
    protected IRI getBoxIri(IRI pageIri, Box box)
    {
        if (box instanceof RDFBox)
            return ((RDFBox) box).getIri();
        else
            return getIriFactory().createBoxURI(pageIri, box);
    }

    /**
     * Reuses an area IRI or creates a new one if the box is not an RDFArea.
     * @param areaTreeIri the IRI of the area tree the box belongs to
     * @param area the area itself
     * @return the IRI
     */
    protected IRI getAreaIri(IRI areaTreeIri, Area area)
    {
        if (area instanceof RDFArea)
            return ((RDFArea) area).getIri();
        else
            return getIriFactory().createAreaURI(areaTreeIri, area);
    }

}
