/**
 * RDFConnectionSet.java
 *
 * Created on 27. 12. 2021, 19:53:54 by burgetr
 */
package cz.vutbr.fit.layout.rdf.model;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.impl.DefaultConnectionSet;

/**
 * 
 * @author burgetr
 */
public class RDFConnectionSet extends DefaultConnectionSet implements RDFResource, RDFArtifact
{
    private Set<Statement> additionalStatements;

    public RDFConnectionSet(IRI parentIri)
    {
        super(parentIri);
    }

    public RDFConnectionSet(IRI parentIri, IRI sourceIri)
    {
        super(parentIri);
        setSourceIri(sourceIri);
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
        // nothing needs to be recomputed at the moment
    }

}
