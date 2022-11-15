/**
 * Attribute.java
 *
 * Created on 14. 11. 2020, 15:52:56 by burgetr
 */
package cz.vutbr.fit.layout.json.parser;

/**
 * 
 * @author burgetr
 */
public class Attribute
{
    public String name;
    public String value;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
}
