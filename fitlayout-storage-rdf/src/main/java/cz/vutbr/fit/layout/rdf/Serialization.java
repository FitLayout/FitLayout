/**
 * Serialization.java
 *
 * Created on 4. 9. 2020, 13:55:39 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.io.OutputStream;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;

/**
 * 
 * @author burgetr
 */
public class Serialization
{
    public static final String JSONLD = "application/ld+json";
    public static final String TURTLE = "text/turtle";
    
    
    public static String colorString(Color color)
    {
        if (color.getAlpha() == 255)
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        else
            return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color decodeHexColor(String colorStr) 
    {
        if (colorStr.length() == 7)
            return new Color(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
        else if (colorStr.length() == 9)
            return new Color(
                    Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                    Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                    Integer.valueOf( colorStr.substring( 5, 7 ), 16 ),
                    Integer.valueOf( colorStr.substring( 7, 9 ), 16 ) );
        else
            return Color.BLACK; //invalid color, this should not happen
    }
    
    public static String displayTypeString(Box.DisplayType type)
    {
        return type.name().toLowerCase().replace('_', '-');
    }
    
    public static Box.DisplayType decodeDisplayType(String typeStr)
    {
        try {
            return Box.DisplayType.valueOf(typeStr.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private static void configureNamespaces(RDFWriter writer)
    {
        writer.handleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        writer.handleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        writer.handleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
        writer.handleNamespace("b", "http://fitlayout.github.io/ontology/render.owl#");
        writer.handleNamespace("a", "http://fitlayout.github.io/ontology/segmentation.owl#");
        writer.handleNamespace("fl", "http://fitlayout.github.io/ontology/fitlayout.owl#");
        writer.handleNamespace("r", "http://fitlayout.github.io/resource/");
    }
    
    public static RDFWriter createRioWriterJsonLD(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
        writer.getWriterConfig().set(JSONLDSettings.OPTIMIZE, true);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }

    public static RDFWriter createRioWriterTurtle(OutputStream os) throws RDFHandlerException
    {
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, os);
        writer.startRDF();
        configureNamespaces(writer);
        writer.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true);
        return writer;
    }
    
    public static void modelToStream(Model model, OutputStream os, String mimeType)
    {
        RDFWriter rdfw;
        switch (mimeType)
        {
            case TURTLE:
                rdfw = createRioWriterTurtle(os);
                break;
            default:
                rdfw = createRioWriterJsonLD(os);
                break;
        }
        for (Statement stmt : model)
            rdfw.handleStatement(stmt);
        rdfw.endRDF();
    }
}
