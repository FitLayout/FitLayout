/**
 * Operation.java
 *
 * Created on 14. 1. 2015, 13:51:30 by burgetr
 */
package cz.vutbr.fit.layout.process.js;

import cz.vutbr.fit.layout.api.AreaTreeOperator;

/**
 * 
 * @author burgetr
 */
public class Operation
{
    private AreaTreeOperator op;
    

    public Operation(AreaTreeOperator op)
    {
        super();
        this.op = op;
    }

    public AreaTreeOperator getOp()
    {
        return op;
    }
    
    
}
