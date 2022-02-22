/**
 * RDFOrderedResource.java
 *
 * Created on 20. 2. 2022, 15:43:43 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

/**
 * A RDF resource that can be ordered.
 * 
 * @author burgetr
 */
public interface RDFOrderedResource extends RDFResource
{

    /**
     * Gets the order of the resource within the artifact.
     * 
     * @return a positive integer that can be used for ordering the resource.
     */
    public int getDocumentOrder();
    

    /**
     * Sets the document order for the resource.
     * 
     * @param order the order (positive integer) to be set.
     */
    public void setDocumentOrder(int order);

}
