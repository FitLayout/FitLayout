/**
 * CliCommand.java
 *
 * Created on 12. 2. 2021, 20:22:31 by burgetr
 */
package cz.vutbr.fit.layout.tools;

/**
 * 
 * @author burgetr
 */
public class CliCommand
{
    private Cli cli;

    public Cli getCli()
    {
        return cli;
    }

    public void setCli(Cli cli)
    {
        this.cli = cli;
    }
    
    protected void printError(String err)
    {
        System.err.println(err);
    }
    
    protected void errNoPage(String op)
    {
        printError("No page to work with. The 'RENDER' or 'LOAD' command must be used before " + op + ".");
    }

    protected void errNoAreaTree(String op)
    {
        printError("No area tree to work with. The 'SEGMENT' or 'LOAD' command must be used before " + op + ".");
    }

    protected void errNoRepo()
    {
        printError("No artifact repository is opened. Try the USE command for opening a repository.");
    }

}
