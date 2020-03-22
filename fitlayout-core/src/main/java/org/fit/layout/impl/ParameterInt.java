/**
 * ParameterInt.java
 *
 * Created on 1. 12. 2017, 14:02:02 by burgetr
 */
package org.fit.layout.impl;

/**
 * 
 * @author burgetr
 */
public class ParameterInt extends BaseParameter
{
    private int minValue;
    private int maxValue;
    
    public ParameterInt(String name, int minValue, int maxValue)
    {
        super(name);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ParameterInt(String name)
    {
        this(name, -1000, 1000);
    }

    public int getMinValue()
    {
        return minValue;
    }

    public int getMaxValue()
    {
        return maxValue;
    }

}
