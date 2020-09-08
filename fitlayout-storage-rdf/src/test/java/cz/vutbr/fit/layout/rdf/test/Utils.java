/**
 * Utils.java
 *
 * Created on 8. 9. 2020, 12:59:17 by burgetr
 */
package cz.vutbr.fit.layout.rdf.test;

import java.util.Scanner;

/**
 * 
 * @author burgetr
 */
public class Utils
{
    
    public static String loadResource(String filePath)
    {
        try (Scanner scanner = new Scanner(Utils.class.getResourceAsStream(filePath), "UTF-8")) {
            scanner.useDelimiter("\\A");
            return scanner.next();
        }
    }

}
