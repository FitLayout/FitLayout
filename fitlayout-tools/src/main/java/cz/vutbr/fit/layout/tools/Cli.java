/**
 * Cli.java
 *
 * Created on 7. 2. 2021, 9:29:03 by burgetr
 */
package cz.vutbr.fit.layout.tools;

/**
 * 
 * @author burgetr
 */
public class Cli
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        Renderer renderer = new Renderer();
        
        renderer.invoke(args);

    }

}
