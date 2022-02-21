/**
 * RDFArea.java
 *
 * Created on 17. 1. 2016, 16:31:01 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Collections;
import java.util.Comparator;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class RDFArea extends DefaultArea implements RDFOrderedResource
{
    protected IRI iri;
    protected int documentOrder;

    public RDFArea(Rectangular r, IRI uri)
    {
        super(r);
        setIri(uri);
    }

    @Override
    public IRI getIri()
    {
        return iri;
    }

    public void setIri(IRI uri)
    {
        this.iri = uri;
    }

    @Override
    public int getDocumentOrder()
    {
        return documentOrder;
    }

    @Override
    public void setDocumentOrder(int documentOrder)
    {
        this.documentOrder = documentOrder;
    }
    
    /**
     * Sorts contained boxes in the document order
     */
    public void sortBoxes()
    {
        Collections.sort(getBoxes(), new Comparator<Box>(){
            @Override
            public int compare(Box box1, Box box2)
            {
                if (box1 instanceof RDFBox && box2 instanceof RDFBox)
                    return ((RDFBox) box1).getOrder() - ((RDFBox) box2).getOrder();
                else
                    return 0;
            }
        });
    }

}
