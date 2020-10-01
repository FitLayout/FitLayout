package cz.vutbr.fit.layout.rdf;
import java.util.AbstractMap;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * ModelLoader.java
 *
 * Created on 17. 1. 2016, 17:19:32 by burgetr
 */

/**
 * Model loader base.
 * @author burgetr
 */
public abstract class ModelLoaderBase
{
    private static Logger log = LoggerFactory.getLogger(ModelLoaderBase.class);
    
    protected Border createBorder(Model model, IRI uri)
    {
        Border ret = new Border();
        
        for (Statement st : model.filter(uri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            
            if (BOX.borderColor.equals(pred)) 
            {
                ret.setColor(Serialization.decodeHexColor(value.stringValue()));
            }
            else if (BOX.borderWidth.equals(pred))
            {
                if (value instanceof Literal)
                    ret.setWidth(((Literal) value).intValue());
            }
            else if (BOX.borderStyle.equals(pred))
            {
                String style = value.stringValue();
                try {
                    ret.setStyle(Border.Style.valueOf(style));
                } catch (IllegalArgumentException r) {
                    log.error("Invalid style value: {}", style);
                }
            }
        }
        
        return ret;
    }
    
    protected Map.Entry<String, String> createAttribute(Model model, IRI uri)
    {
        String name = null;
        String avalue = null;
        for (Statement st : model.filter(uri, null, null))
        {
            final IRI pred = st.getPredicate();
            final Value value = st.getObject();
            if (RDFS.LABEL.equals(pred))
            {
                if (value instanceof Literal)
                    name = ((Literal) value).stringValue();
            }
            else if (RDF.VALUE.equals(pred))
            {
                if (value instanceof Literal)
                    avalue = ((Literal) value).stringValue();
            }
        }
        if (name != null && avalue != null)
            return new AbstractMap.SimpleEntry<String, String>(name, avalue);
        else
            return null;
    }
    
}
