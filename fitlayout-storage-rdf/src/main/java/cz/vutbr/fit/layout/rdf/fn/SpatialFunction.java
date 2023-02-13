/**
 * SpatialFunction.java
 *
 * Created on 13. 2. 2023, 13:35:38 by burgetr
 */
package cz.vutbr.fit.layout.rdf.fn;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;

import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * A base for all spatial functions.
 * 
 * @author burgetr
 */
public abstract class SpatialFunction
{
    
    /**
     * Finds the bounds resource: either the b:bounds property is set for the object or we use the object itself.
     * @param tripleSource
     * @param arg
     * @return
     * @throws ValueExprEvaluationException
     */
    protected Resource getBoundsResource(TripleSource tripleSource, Value arg) throws ValueExprEvaluationException
    {
        if (arg instanceof Resource)
        {
            Resource rect = null;
            final Resource box = (Resource) arg;
            var blist = QueryResults.asList(tripleSource.getStatements(box, BOX.bounds, null));
            if (!blist.isEmpty()) // there is the b:bounds value
            {
                Value rectValue = blist.get(0).getObject();
                if (rectValue instanceof Resource)
                    rect = (Resource) rectValue; // we will use the b:bounds value
            }
            else
                rect = (Resource) arg; // we will try to use the argument object itself
            
            return rect;
        }
        else
            throw new ValueExprEvaluationException("invalid argument (resource expected): " + arg);
    }

    protected Rectangular getRectangularValue(TripleSource tripleSource, Resource subj)
    {
        Integer x = getIntValue(tripleSource, subj, BOX.positionX);
        Integer y = getIntValue(tripleSource, subj, BOX.positionY);
        Integer w = getIntValue(tripleSource, subj, BOX.width);
        Integer h = getIntValue(tripleSource, subj, BOX.height);
        if (x != null && y != null && w != null && h != null)
            return new Rectangular(x, y, x + w - 1, y + h -1);
        else
            return null;
    }
    
    protected Integer getIntValue(TripleSource tripleSource, Resource subj, IRI property)
    {
        var blist = QueryResults.asList(tripleSource.getStatements(subj, property, null));
        if (!blist.isEmpty()) // there is the b:bounds value
        {
            Value val = blist.get(0).getObject();
            if (val instanceof Literal)
            {
                try {
                    return ((Literal) val).intValue();
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            else
                return null;
        }
        else
            return null;
    }

}
