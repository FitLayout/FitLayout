/**
 * Cli.java
 *
 * Created on 7. 2. 2021, 9:29:03 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 
 * @author burgetr
 */
@Command(name = "fitlayout", subcommands = {Renderer.class, Segmentator.class})
public class Cli
{

    private static List<List<String>> splitArgsByCommands(String[] args, Set<String> cnames)
    {
        List<List<String>> ret = new ArrayList<>();
        List<String> current = new ArrayList<>();
        ret.add(current);
        for (int i = 0; i < args.length; i++)
        {
            if (cnames.contains(args[i]) && !current.isEmpty())
            {
                current = new ArrayList<>();
                ret.add(current);
            }
            current.add(args[i]);
        }
        return ret;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        CommandLine cmd = new CommandLine(new Cli());
        cmd.setUsageHelpWidth(90);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        System.out.println(cmd.getCommandSpec().userObject());
        
        //split command line to individual commands
        Set<String> cnames = cmd.getSubcommands().keySet();
        List<List<String>> subcommands = splitArgsByCommands(args, cnames);
        
        //execute the individual command lines
        for (List<String> subcl : subcommands)
        {
            String[] a = subcl.toArray(new String[0]);
            int exitCode = cmd.execute(a);
            if (exitCode != 0)
                System.exit(exitCode);
        }
    }

}
