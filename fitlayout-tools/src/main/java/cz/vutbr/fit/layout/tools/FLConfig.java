/**
 * FLConfig.java
 *
 * Created on 3. 10. 2020, 23:09:57 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.bcs.BCSProvider;
import cz.vutbr.fit.layout.cormier.CormierProvider;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.map.chunks.MetadataTextChunksProvider;
import cz.vutbr.fit.layout.map.op.TagByExamplesOperator;
import cz.vutbr.fit.layout.patterns.AreaConnectionProvider;
import cz.vutbr.fit.layout.patterns.TextChunkConnectionProvider;
import cz.vutbr.fit.layout.patterns.TextChunkDOMLinkProvider;
import cz.vutbr.fit.layout.pdf.PDFBoxTreeProvider;
import cz.vutbr.fit.layout.playwright.PlaywrightTreeProvider;
import cz.vutbr.fit.layout.provider.OperatorWrapperProvider;
import cz.vutbr.fit.layout.provider.VisualBoxTreeProvider;
import cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFTaggerConfig;
import cz.vutbr.fit.layout.rdf.text.CompleteTextProvider;
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
import cz.vutbr.fit.layout.segm.op.TagByAttributeOperator;
import cz.vutbr.fit.layout.text.chunks.LeafAreaChunksProvider;
import cz.vutbr.fit.layout.text.chunks.TextChunksProvider;
import cz.vutbr.fit.layout.text.chunks.WordsChunksProvider;
import cz.vutbr.fit.layout.text.op.TagEntitiesOperator;
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
    public static ServiceManager createServiceManager(ArtifactRepository repo)
    {
        //initialize the services
        ServiceManager sm = ServiceManager.create();
        
        //renderers
        sm.addArtifactService(new CSSBoxTreeProvider());
        sm.addArtifactService(new PuppeteerTreeProvider());
        sm.addArtifactService(new PlaywrightTreeProvider());
        sm.addArtifactService(new PDFBoxTreeProvider());
        
        //visual box tree construction
        sm.addArtifactService(new VisualBoxTreeProvider());
        
        //segmentation
        sm.addArtifactService(new BasicSegmProvider());
        sm.addArtifactService(new VipsProvider());
        sm.addArtifactService(new BCSProvider());
        sm.addArtifactService(new CormierProvider());
        
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
        addAreaTreeOperator(sm, new TagByAttributeOperator());
        
        //simple text chunks extractors
        sm.addArtifactService(new LeafAreaChunksProvider());
        sm.addArtifactService(new WordsChunksProvider());
        
        //text module when the tags are configured in the repo metadata
        if (repo != null && repo instanceof RDFArtifactRepository)
        {
            RDFTaggerConfig tc = new RDFTaggerConfig((RDFArtifactRepository) repo);
            //log.info("Using taggers from RDF: {}", tc.getTaggers());
            // configure text module using RDF taggers
            sm.addArtifactService(new TextChunksProvider(tc));
            TagEntitiesOperator tagOp = new TagEntitiesOperator();
            tagOp.setTaggerConfig(tc);
            addAreaTreeOperator(sm, tagOp);
        }
        
        //metadata tagging
        if (repo != null)
        {
            addAreaTreeOperator(sm, new TagByExamplesOperator());
        }

        //text module
        /*FixedTaggerConfig tagConfig = new FixedTaggerConfig();
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "date"), new DateTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "time"), new TimeTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "persons"), new PersonsTagger());
        tagConfig.setTagger(new DefaultTag("FitLayout.TextTag", "locations"), new LocationsTagger());
        sm.addArtifactService(new TextChunksProvider(tagConfig));
        TagEntitiesOperator tagOp = new TagEntitiesOperator();
        tagOp.setTaggers(tagConfig.getTaggers());
        addAreaTreeOperator(sm, tagOp);*/
        
        //page metadata-based text chunks
        sm.addArtifactService(new MetadataTextChunksProvider());
        
        //patterns
        sm.addArtifactService(new AreaConnectionProvider());
        sm.addArtifactService(new TextChunkConnectionProvider());
        sm.addArtifactService(new TextChunkDOMLinkProvider());
        
        //complete text extension
        sm.addArtifactService(new CompleteTextProvider());
        
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
