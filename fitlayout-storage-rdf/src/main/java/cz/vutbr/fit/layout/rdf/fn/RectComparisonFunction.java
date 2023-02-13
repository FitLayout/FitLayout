/**
 * RectComparisonFunction.java
 *
 * Created on 13. 2. 2023, 15:15:46 by burgetr
 */
package cz.vutbr.fit.layout.rdf.fn;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.ontology.FLFN;

/**
 * 
 * @author burgetr
 */
public abstract class RectComparisonFunction extends SpatialFunction implements Function
{
    private String fname;
    
    public RectComparisonFunction(String fname)
    {
        this.fname = fname;
    }

    @Override
    public String getURI()
    {
        return FLFN.NAMESPACE + fname;
    }
    
    /**
     * Compares the two boxes and returns a boolean result.
     * @param b1
     * @param b2
     * @return
     */
    protected abstract boolean evaluateForBoxes(Rectangular b1, Rectangular b2);
    
    /**
     * Executes the palindrome function.
     *
     * @return A boolean literal representing true if the input argument boxes are below each other.
     * @throws ValueExprEvaluationException
     */
    public Value evaluate(TripleSource tripleSource, Value... args) throws ValueExprEvaluationException
    {
        if (args.length != 2)
        {
            throw new ValueExprEvaluationException(
                    fname + " function requires" + "exactly 2 arguments, got "
                            + args.length);
        }
        
        Resource rect1 = getBoundsResource(tripleSource, args[0]);
        Resource rect2 = getBoundsResource(tripleSource, args[1]);
        Rectangular b1 = getRectangularValue(tripleSource, rect1);
        Rectangular b2 = getRectangularValue(tripleSource, rect2);

        /*if (rect1.toString().contains("c1516") && rect2.toString().contains("c1512"))
            System.out.println("jo!");*/
        
        boolean ret = evaluateForBoxes(b1, b2);
        return Values.literal(ret);
    }


    @Override
    public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException
    {
        throw new UnsupportedOperationException();
    }

}
