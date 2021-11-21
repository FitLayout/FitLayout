/**
 * DefaultArtifactRepository.java
 *
 * Created on 10.9.2020, 11:11:17 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.IRIDecoder;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * Default simple in-memory implementation of an ArtifactRepository.
 * 
 * @author burgetr
 */
public class DefaultArtifactRepository implements ArtifactRepository
{
    private static ValueFactory vf = SimpleValueFactory.getInstance();

    private int idCounter;
    private IRIDecoder iriDecoder;
    private Map<IRI, Artifact> repo;
    

    public DefaultArtifactRepository()
    {
        idCounter = 1;
        iriDecoder = new DefaultIRIDecoder();
        repo = new HashMap<>();
    }

    @Override
    public Collection<IRI> getArtifactIRIs()
    {
        return repo.keySet();
    }

    @Override
    public Collection<Artifact> getArtifactInfo()
    {
        return repo.values();
    }

    @Override
    public Artifact getArtifact(IRI artifactIri)
    {
        return repo.get(artifactIri);
    }

    @Override
    public void addArtifact(Artifact artifact)
    {
        if (artifact.getIri() == null)
            artifact.setIri(createArtifactIri(artifact));
        repo.put(artifact.getIri(), artifact);
    }

    @Override
    public IRI createArtifactIri(Artifact artifact)
    {
        String atype;
        if (artifact instanceof Page)
            atype = "boxtree";
        else if (artifact instanceof AreaTree)
            atype = "areatree";
        else if (artifact instanceof LogicalAreaTree)
            atype = "logicaltree";
        else
            atype = "artifact";
        
        return vf.createIRI("urn:" + atype + ":" + String.valueOf(idCounter++));
    }

    @Override
    public void replaceArtifact(IRI artifactIri, Artifact artifact)
    {
        artifact.setIri(artifactIri);
        repo.put(artifactIri, artifact);
    }

    @Override
    public void removeArtifact(IRI artifactIri)
    {
        List<Artifact> derived = new ArrayList<>();
        findDerivedArtifacts(artifactIri, derived);
        for (Artifact a : derived)
            repo.remove(a.getIri());
        repo.remove(artifactIri);
    }
    
    private void findDerivedArtifacts(IRI artifactIri, List<Artifact> dest)
    {
        for (Artifact a : repo.values())
        {
            if (a.getParentIri() == artifactIri)
            {
                findDerivedArtifacts(a.getIri(), dest);
                dest.add(a);
            }
        }
    }

    @Override
    public void clear()
    {
        repo.clear();
    }

    @Override
    public IRIDecoder getIriDecoder()
    {
        return iriDecoder;
    }

    @Override
    public void disconnect()
    {
    }

}
