/**
 * ArtifactInfo.java
 *
 * Created on 30. 10. 2020, 13:08:12 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import cz.vutbr.fit.layout.api.ArtifactInfo;
import cz.vutbr.fit.layout.impl.BaseArtifact;
import cz.vutbr.fit.layout.ontology.FL;

/**
 * Information about an artifact obtained from a RDF model. 
 * 
 * @author burgetr
 */
public class RDFArtifactInfo extends ArtifactInfo
{
    
    public RDFArtifactInfo(Model model, IRI artifactIri) 
    {
        super(null); //the parent IRI is taken from the model below
        setIri(artifactIri);
        for (Statement st : model) 
        {
            if (st.getSubject().equals(artifactIri))
                processStatement(st);
        }
    }

    protected void processStatement(Statement st)
    {
        if (st.getPredicate().equals(RDF.TYPE))
        {
            Value val = st.getObject();
            if (val instanceof IRI)
                setArtifactType((IRI) val);
        }
        else if (st.getPredicate().equals(FL.createdOn))
        {
            Value val = st.getObject();
            if (val instanceof Literal) setCreatedOn(
                    ((Literal) val).calendarValue().toGregorianCalendar().getTime());
        }
        else if (st.getPredicate().equals(FL.hasParentArtifact))
        {
            Value val = st.getObject();
            if (val instanceof IRI)
                setParentIri((IRI) val);
        }
        else if (st.getPredicate().equals(FL.creator))
        {
            setCreator(st.getObject().stringValue());
        }
        else if (st.getPredicate().equals(FL.creatorParams))
        {
            setCreatorParams(st.getObject().stringValue());
        }
    }
    
    public void applyToArtifact(BaseArtifact a)
    {
        a.setIri(getIri());
        a.setParentIri(getParentIri());
        a.setCreatedOn(getCreatedOn());
        a.setCreator(getCreator());
        a.setCreatorParams(getCreatorParams());
    }
    
}
