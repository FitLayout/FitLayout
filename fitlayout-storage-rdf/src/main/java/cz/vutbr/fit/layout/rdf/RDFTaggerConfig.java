/**
 * RDFTaggerConfig.java
 *
 * Created on 5. 1. 2022, 21:28:29 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.ServiceConfig;
import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.api.TaggerConfig;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFTag;

/**
 * A tagger config implementation that uses a ServiceManager and the configured RDF
 * repository as the source of the tagger configuration.
 * 
 * @author burgetr
 */
public class RDFTaggerConfig implements TaggerConfig
{
    private static Logger log = LoggerFactory.getLogger(RDFTaggerConfig.class);

    private RDFArtifactRepository repo;
    private Map<IRI, Tagger> taggerCache;
    
    
    public RDFTaggerConfig(RDFArtifactRepository repo)
    {
        this.repo = repo;
        this.taggerCache = new HashMap<>();
    }

    @Override
    public Map<Tag, Tagger> getTaggers()
    {
        Map<Tag, Tagger> ret = new HashMap<>();
        Collection<RDFTag> tags = repo.getTags();
        for (Tag tag : tags)
        {
            Tagger tagger = getTaggerForTag(tag);
            if (tagger != null)
                ret.put(tag, tagger);
        }
        return ret;
    }

    @Override
    public Tagger getTaggerForTag(Tag tag)
    {
        Value val = repo.getStorage().getPropertyValue(tag.getIri(), SEGM.tagger);
        if (val instanceof IRI)
            return getTaggerForIri((IRI) val);
        else
            return null;
    }
    
    protected Tagger getTaggerForIri(IRI iri)
    {
        if (taggerCache.containsKey(iri))
        {
            return taggerCache.get(iri);
        }
        else
        {
            Tagger tagger = loadTagger(iri);
            if (tagger!= null)
                taggerCache.put(iri, tagger);
            return tagger;
        }
    }

    // =============================================================================
    
    private Tagger loadTagger(IRI taggerIri)
    {
        ServiceConfig conf = repo.getStorage().loadServiceConfig(taggerIri);
        if (conf != null)
        {
            try
            {
                Class<?> tcls = Class.forName(conf.getServiceId());
                Object inst = tcls.getDeclaredConstructor().newInstance();
                if (inst instanceof Tagger)
                {
                    Tagger tagger = (Tagger) inst;
                    for (Map.Entry<String, Object> param : conf.getParams().entrySet())
                        tagger.setParam(param.getKey(), param.getValue());
                    return tagger;
                }
                else
                {
                    log.error("Service {} used in {} is not a tagger", conf.getServiceId(), taggerIri);
                    return null;
                }
            } catch (Exception e) {
                log.error("Couldn't create instance of tagger {} : {}", conf.getServiceId(), e.getMessage());
                return null;
            } 
        }
        else
        {
            log.warn("Couldn't find usable tagger definition: {}", taggerIri);
            return null;
        }
    }

}
