/**
 * NERTagger.java
 *
 * Created on 28. 11. 2015, 0:34:14 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.text.tag.TreeTagger;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;

/**
 * 
 * @author burgetr
 */
public abstract class NERTagger extends BaseTagger
{
    private static Logger log = LoggerFactory.getLogger(NERTagger.class);
    
    private static final String CLASSIFIER_PATH = "/3class.gz";
    
    private static AbstractSequenceClassifier<?> sharedClassifier;

    
    public AbstractSequenceClassifier<?> getClassifier()
    {
        return getSharedClassifier();
    }
    
    //============================================================================
    
    public static AbstractSequenceClassifier<?> getSharedClassifier()
    {
        if (sharedClassifier == null)
        {
            log.info("Loading resource {}", TreeTagger.class.getResource(CLASSIFIER_PATH));
            InputStream is;
            try
            {
                is = new GZIPInputStream(TreeTagger.class.getResourceAsStream(CLASSIFIER_PATH));
                sharedClassifier = CRFClassifier.getClassifier(is);
            } catch (IOException e)
            {
                log.error("Load failed: {}", e.getMessage());
            } catch (ClassCastException e)
            {
                log.error("Load failed: {}", e.getMessage());
            } catch (ClassNotFoundException e)
            {
                log.error("Load failed: {}", e.getMessage());
            }
        }
        return sharedClassifier;
    }

    
}
