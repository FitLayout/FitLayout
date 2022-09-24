/**
 * ParameterFloat.java
 *
 * Created on 1. 12. 2017, 14:04:34 by burgetr
 */
package cz.vutbr.fit.layout.impl;

/**
 * 
 * @author burgetr
 */
public class ParameterFloat extends BaseParameter
{
    private float minValue;
    private float maxValue;
    
    public ParameterFloat(String name, String description, float minValue, float maxValue)
    {
        super(name, description);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public ParameterFloat(String name, float minValue, float maxValue)
    {
        super(name);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public ParameterFloat(String name)
    {
        this(name, -1000.0f, 1000.0f);
    }

    public float getMinValue()
    {
        return minValue;
    }

    public float getMaxValue()
    {
        return maxValue;
    }

}
