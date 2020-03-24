/**
 * BaseParametrizedOperation.java
 *
 * Created on 27. 1. 2015, 15:22:03 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ParametrizedOperation;

/**
 * 
 * @author burgetr
 */
public abstract class BaseParametrizedOperation implements ParametrizedOperation
{
    private static Logger log = LoggerFactory.getLogger(BaseParametrizedOperation.class);
    
    private List<Parameter> params;
    
    @Override
    public List<Parameter> getParams()
    {
        if (params == null)
            params = defineParams();
        return params;
    }

    /**
     * Creates the parameter definition for this operation.
     * @return The list of parameters.
     */
    public List<Parameter> defineParams()
    {
        //The default implementation returns no parameters
        return Collections.emptyList();
    }
    
    /**
     * Sets the parameter using the appropriate setter method (if present).
     */
    @Override
    public boolean setParam(String name, Object value)
    {
        String sname = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method m;
        try {
            if (value instanceof Integer)
            {
                m = getClass().getMethod(sname, int.class);
                m.invoke(this, value);
            }
            else if (value instanceof Double)
            {
                try {
                    m = getClass().getMethod(sname, float.class);
                    m.invoke(this, ((Double) value).floatValue());
                } catch (NoSuchMethodException e) {
                    //no float version found, try the int version
                    m = getClass().getMethod(sname, int.class);
                    m.invoke(this, ((Double) value).intValue());
                }
            }
            else if (value instanceof Float)
            {
                try {
                    m = getClass().getMethod(sname, float.class);
                    m.invoke(this, value);
                } catch (NoSuchMethodException e) {
                    //no float version found, try the int version
                    m = getClass().getMethod(sname, int.class);
                    m.invoke(this, ((Double) value).intValue());
                }
            }
            else if (value instanceof Boolean)
            {
                m = getClass().getMethod(sname, boolean.class);
                m.invoke(this, value);
            }
            else
            {
                m = getClass().getMethod(sname, String.class);
                m.invoke(this, value.toString());
            }
            return true;
            
        } catch (NoSuchMethodException e) {
            log.warn("Setting unknown parameter: " + e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtains the parameter using the appropriate getter method (if present).
     */
    @Override
    public Object getParam(String name)
    {
        String sname = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        try
        {
            Method m = getClass().getMethod(sname);
            return m.invoke(this);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
