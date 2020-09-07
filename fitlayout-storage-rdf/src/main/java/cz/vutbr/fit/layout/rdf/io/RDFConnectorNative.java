/**
 * RDFConnectorNative.java
 *
 * Created on 7. 9. 2020, 13:20:47 by burgetr
 */
package cz.vutbr.fit.layout.rdf.io;

import java.io.File;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

/**
 * 
 * @author burgetr
 */
public class RDFConnectorNative extends RDFConnector
{
    private String dataDir;
    
    
    public RDFConnectorNative(String dataDir)
    {
        this.dataDir = dataDir;
        initRepository();
    }

    public String getDataDir()
    {
        return dataDir;
    }

    @Override
    protected void initRepository() throws RepositoryException
    {
        File data = new File(dataDir);
        repo = new SailRepository(new NativeStore(data));
        connection = repo.getConnection();
    }

}
