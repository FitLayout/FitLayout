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
    
    public RDFTaggerConfig(RDFArtifactRepository repo)
    {
        this.repo = repo;
    }

    @Override
    public Map<Tag, Tagger> getTaggers()
    {
        Map<Tag, Tagger> ret = new HashMap<>();
        Collection<Tag> tags = repo.getTags();
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
        if (tag instanceof RDFTag)
        {
            Value val = repo.getStorage().getPropertyValue(((RDFTag) tag).getIri(), SEGM.tagger);
            if (val instanceof IRI)
                return loadTagger((IRI) val);
            else
                return null;
        }
        else
        {
            log.error("Cannot get tagger for a non-RDF tag {}", tag);
            return null;
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
                Object ret = tcls.getDeclaredConstructor().newInstance();
                return (Tagger) ret;
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
