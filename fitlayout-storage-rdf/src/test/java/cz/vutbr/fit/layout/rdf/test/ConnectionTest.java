/**
 * ConnectionTest.java
 *
 * Created on 7. 9. 2020, 14:03:01 by burgetr
 */
package cz.vutbr.fit.layout.rdf.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.junit.Test;

import cz.vutbr.fit.layout.rdf.RDFStorage;

/**
 * 
 * @author burgetr
 */
public class ConnectionTest
{
    private static String[] owls = new String[] {"render.owl", "segmentation.owl", "fitlayout.owl", "mapping.owl"};
    

    @Test
    public void checkStorageConnection() throws RDFParseException, RepositoryException, IOException
    {
        RDFStorage storage = RDFStorage.createMemory(null);
        
        //load the ontologies
        for (String owl : owls)
        {
            String owlFile = Utils.loadResource("/rdf/" + owl);
            storage.importXML(owlFile);
        }
        
        //load testing artifacts
        String page = Utils.loadResource("/rdf/page.ttl");
        storage.importTurtle(page);
        Set<IRI> pages = storage.getArtifactIRIs();
        
        assertEquals("One page has been loaded", 1, pages.size());
    }
    
}
