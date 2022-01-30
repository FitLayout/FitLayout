/**
 * FLConfig.java
 *
 * Created on 3. 10. 2020, 23:09:57 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.bcs.BCSProvider;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.patterns.AreaConnectionProvider;
import cz.vutbr.fit.layout.patterns.TextChunkConnectionProvider;
import cz.vutbr.fit.layout.provider.OperatorWrapperProvider;
import cz.vutbr.fit.layout.provider.VisualBoxTreeProvider;
import cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.segm.BasicSegmProvider;
import cz.vutbr.fit.layout.segm.op.CollapseAreasOperator;
import cz.vutbr.fit.layout.segm.op.FindLineOperator;
import cz.vutbr.fit.layout.segm.op.FlattenTreeOperator;
import cz.vutbr.fit.layout.segm.op.GroupByDOMOperator;
import cz.vutbr.fit.layout.segm.op.HomogeneousLeafOperator;
import cz.vutbr.fit.layout.segm.op.MultiLineOperator;
import cz.vutbr.fit.layout.segm.op.SortByLinesOperator;
import cz.vutbr.fit.layout.segm.op.SortByPositionOperator;
import cz.vutbr.fit.layout.segm.op.SuperAreaOperator;
import cz.vutbr.fit.layout.text.chunks.TextChunksProvider;
import cz.vutbr.fit.layout.text.op.TagEntitiesOperator;
import cz.vutbr.fit.layout.text.tag.FixedTaggerConfig;
import cz.vutbr.fit.layout.text.taggers.DateTagger;
import cz.vutbr.fit.layout.text.taggers.LocationsTagger;
import cz.vutbr.fit.layout.text.taggers.PersonsTagger;
import cz.vutbr.fit.layout.text.taggers.TimeTagger;
import cz.vutbr.fit.layout.vips.VipsProvider;

/**
 * FitLayout configuration utilities.
 * 
 * @author burgetr
 */
public class FLConfig
{
    //private static Logger log = LoggerFactory.getLogger(FLConfig.class);

    /**
     * Creates and configures a FitLayout ServiceManager instance.
     * @param repo the artifact repository to be used by the service manager or {@code null} when
     * no repository should be configured.
     * @return the created ServiceManager instance
     */
    public static ServiceManager createServiceManager(RDFArtifactRepository repo)
    {
        //initialize the services
        ServiceManager sm = ServiceManager.create();
        
        //renderers
        sm.addArtifactService(new CSSBoxTreeProvider());
        sm.addArtifactService(new PuppeteerTreeProvider());
        
        //visual box tree construction
        sm.addArtifactService(new VisualBoxTreeProvider());
        
        //segmentation
        sm.addArtifactService(new BasicSegmProvider());
        sm.addArtifactService(new VipsProvider());
        sm.addArtifactService(new BCSProvider());
        
        //standard operators
        addAreaTreeOperator(sm, new CollapseAreasOperator());
        addAreaTreeOperator(sm, new FindLineOperator());
        addAreaTreeOperator(sm, new FlattenTreeOperator());
        addAreaTreeOperator(sm, new MultiLineOperator());
        addAreaTreeOperator(sm, new SortByPositionOperator());
        addAreaTreeOperator(sm, new SortByLinesOperator());
        addAreaTreeOperator(sm, new SuperAreaOperator());
        addAreaTreeOperator(sm, new GroupByDOMOperator());
        addAreaTreeOperator(sm, new HomogeneousLeafOperator());
        
        /*if (repo != null)
        {
            RDFTaggerConfig tc = new RDFTaggerConfig(repo);
            log.info("Using taggers from RDF: {}", tc.getTaggers());
            // configure text module using RDF taggers
            sm.addArtifactService(new TextChunksProvider(tc));
            TagEntitiesOperator tagOp = new TagEntitiesOperator();
            tagOp.setTaggers(tc.getTaggers());
            addAreaTreeOperator(sm, tagOp);
        }*/

        //text module
        FixedTaggerConfig tagConfig = new FixedTaggerConfig();
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "date"), new DateTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "time"), new TimeTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "persons"), new PersonsTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "locations"), new LocationsTagger());
        sm.addArtifactService(new TextChunksProvider(tagConfig));
        TagEntitiesOperator tagOp = new TagEntitiesOperator();
        tagOp.setTaggers(tagConfig.getTaggers());
        addAreaTreeOperator(sm, tagOp);
        
        //patterns
        sm.addArtifactService(new AreaConnectionProvider());
        sm.addArtifactService(new TextChunkConnectionProvider());
        
        //use RDF storage as the artifact repository
        if (repo != null)
            sm.setArtifactRepository(repo);
        return sm;
    }

    /**
     * Adds an area tree operator and the corresponding wrapping artifact provider
     * to the service manager.
     * @param sm
     * @param op
     */
    private static void addAreaTreeOperator(ServiceManager sm, AreaTreeOperator op)
    {
        sm.addAreaTreeOperator(op);
        sm.addArtifactService(new OperatorWrapperProvider(op));
    }
    
}
