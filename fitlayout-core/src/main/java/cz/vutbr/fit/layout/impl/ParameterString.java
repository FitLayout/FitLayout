/**
 * ParameterString.java
 *
 * Created on 1. 12. 2017, 14:06:23 by burgetr
 */
package cz.vutbr.fit.layout.impl;

/**
 * 
 * @author burgetr
 */
public class ParameterString extends BaseParameter
{
    private int minLength;
    private int maxLength;
    
    public ParameterString(String name, String description, int minLength, int maxLength)
    {
        super(name, description);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    public ParameterString(String name, int minLength, int maxLength)
    {
        super(name);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    public ParameterString(String name)
    {
        this(name, 0, 10);
    }

    public int getMinLength()
    {
        return minLength;
    }

    public int getMaxLength()
    {
        return maxLength;
    }
    
}
