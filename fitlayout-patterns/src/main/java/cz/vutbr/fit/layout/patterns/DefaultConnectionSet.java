/**
 * DefaultConnectionSet.java
 *
 * Created on 26. 12. 2021, 19:19:18 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.impl.BaseArtifact;
import cz.vutbr.fit.layout.model.AreaConnection;
import cz.vutbr.fit.layout.model.ConnectionSet;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * 
 * @author burgetr
 */
public class DefaultConnectionSet extends BaseArtifact implements ConnectionSet
{
    private IRI sourceIri;
    private Set<AreaConnection> areaConnections;
    

    public DefaultConnectionSet(IRI parentIri)
    {
        super(parentIri);
        setSourceIri(parentIri);
    }

    @Override
    public IRI getArtifactType()
    {
        return BOX.ConnectionSet;
    }

    @Override
    public IRI getSourceIri()
    {
        return sourceIri;
    }

    public void setSourceIri(IRI sourceIri)
    {
        this.sourceIri = sourceIri;
    }

    @Override
    public Set<AreaConnection> getAreaConnections()
    {
        return areaConnections;
    }

    public void setAreaConnections(Set<AreaConnection> areaConnections)
    {
        this.areaConnections = areaConnections;
    }

}
