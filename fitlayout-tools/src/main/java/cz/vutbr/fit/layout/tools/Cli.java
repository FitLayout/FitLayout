/**
 * Cli.java
 *
 * Created on 7. 2. 2021, 9:29:03 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "fitlayout", subcommands = {Renderer.class, Segmentator.class})
public class Cli
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //Renderer renderer = new Renderer();
        
        CommandLine cmd = new CommandLine(new Cli());
        cmd.setUsageHelpWidth(90);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

}
