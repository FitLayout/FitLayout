/**
 * DefaultArtifactRepository.java
 *
 * Created on 10.9.2020, 11:11:17 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.model.Artifact;

/**
 * Default simple in-memory implementation of an ArtifactRepository
 * @author burgetr
 */
public class DefaultArtifactRepository implements ArtifactRepository
{
    private static ValueFactory vf = SimpleValueFactory.getInstance();

    private int idCounter;
    private Map<IRI, Artifact> repo;
    

    public DefaultArtifactRepository()
    {
        idCounter = 1;
        repo = new HashMap<>();
    }

    @Override
    public Artifact getArtifact(IRI artifactIri)
    {
        return repo.get(artifactIri);
    }

    @Override
    public void addArtifact(Artifact artifact)
    {
        repo.put(artifact.getIri(), artifact);
    }

    @Override
    public IRI createArtifactIri(Artifact artifact)
    {
        return vf.createIRI("urn:artifact:" + String.valueOf(idCounter++));
    }

}
