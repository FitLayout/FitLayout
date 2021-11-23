/**
 * ArtifactInfo.java
 *
 * Created on 30. 10. 2020, 13:08:12 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

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
    /**
     * Additional statements that do not influence the properties of the artifact itself
     * but should be preserved together with the artifact.
     */
    private Set<Statement> additionalStatements;
    
    public RDFArtifactInfo(Model model, IRI artifactIri) 
    {
        super(null); //the parent IRI is taken from the model below
        setIri(artifactIri);
        additionalStatements = new HashSet<>();
        for (Statement st : model) 
        {
            if (st.getSubject().equals(artifactIri))
            {
                if (!processStatement(st))
                    additionalStatements.add(st);
            }
        }
    }

    public Set<Statement> getAdditionalStatements()
    {
        return additionalStatements;
    }

    /**
     * Processes a model statement and changes the artifact accordingly.
     * @param st the statement to process
     * @return {@code true} if the statement was used for changing the model, {@code false} when the statement was ignored.
     */
    protected boolean processStatement(Statement st)
    {
        boolean ret = true;
        if (st.getPredicate().equals(RDF.TYPE))
        {
            Value val = st.getObject();
            if (val instanceof IRI)
                setArtifactType((IRI) val);
        }
        else if (st.getPredicate().equals(RDFS.LABEL))
        {
            setLabel(st.getObject().stringValue());
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
        else
            ret = false;
        return ret;
    }
    
    public void applyToArtifact(BaseArtifact a)
    {
        a.setIri(getIri());
        a.setParentIri(getParentIri());
        a.setLabel(getLabel());
        a.setCreatedOn(getCreatedOn());
        a.setCreator(getCreator());
        a.setCreatorParams(getCreatorParams());
    }
    
}
