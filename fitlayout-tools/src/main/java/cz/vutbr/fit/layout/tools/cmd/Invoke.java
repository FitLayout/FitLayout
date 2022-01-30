/**
 * Invoke.java
 *
 * Created on 30. 1. 2022, 11:35:59 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A command-line interface to a generic artifact service invocation.
 * 
 * @author burgetr
 */
@Command(name = "INVOKE", sortOptions = false, abbreviateSynopsis = true,
    description = "Invokes an artifact service on the last artifact")
public class Invoke extends CliCommand implements Callable<Integer>
{
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-s", "--service"}, description = "ID of the service to invoke", required = true)
    protected String serviceId;
    
    @Option(order = 2, names = {"-O", "--options"}, paramLabel = "KEY=VALUE", split = "\\,", splitSynopsisLabel = ",", description = "Segmentation method options")
    protected Map<String, String> sopts;

    @Override
    public Integer call() throws Exception
    {
        try {
            Artifact srcArt = getCli().getLastArtifact();
            if (srcArt == null) {
                errNoArtifact("INVOKE");
                return 1;
            }
            
            Artifact result = invokeService(srcArt, serviceId, sopts);
            getCli().setLastArtifact(result);
            System.err.println("  Created: " + result);
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
        }
        
        return 1;
    }

    public Artifact invokeService(Artifact srcArt, String serviceId, Map<String, String> sopts)
    {
        
        ParametrizedOperation op = getCli().getServiceManager().findParmetrizedService(serviceId);
        System.err.println("Invoke: " + op);
        if (op != null)
        {
            if (sopts != null)
            {
                Map<String, Object> sparams = new HashMap<>(sopts);
                ServiceManager.setServiceParams(op, sparams);
            }
            
            System.err.println("  Params: " + op.getParamString());
            Artifact result = ((ArtifactService) op).process(srcArt);
            return result;
        }
        else
        {
            return null;
        }
    }

}
