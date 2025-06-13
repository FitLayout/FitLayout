/**
 * SparqlQueryResult.java
 *
 * Created on 16. 11. 2021, 18:52:15 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.List;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * A result of a generic SPARQL query that holds the result value(s) depending
 * on the query type.
 * 
 * @author burgetr
 */
public class SparqlQueryResult
{
    public enum Type { TUPLE, GRAPH, BOOLEAN };
    
    private TupleResult tupleResult;
    private List<Statement> graphResult;
    private boolean booleanResult;
    private Type type;
    
    /**
     * Creates a tuple query result from a list of bindings.
     * @param tuples the bindings
     * @return the new query result
     */
    public static SparqlQueryResult createTuple(List<String> bindingNames, List<BindingSet> tuples)
    {
        SparqlQueryResult ret = new SparqlQueryResult();
        ret.tupleResult = new TupleResult(bindingNames, tuples);
        ret.type = Type.TUPLE;
        return ret;
    }

    /**
     * Creates a graph query result from a list of statements.
     * @param graphResult the statements
     * @return the new query result
     */
    public static SparqlQueryResult createGraph(List<Statement> graphResult)
    {
        SparqlQueryResult ret = new SparqlQueryResult();
        ret.graphResult = graphResult;
        ret.type = Type.GRAPH;
        return ret;
    }

    /**
     * Creates a boolean query result from a boolean value.
     * @param booleanResult the result value
     * @return the new query result
     */
    public static SparqlQueryResult createBoolean(boolean booleanResult)
    {
        SparqlQueryResult ret = new SparqlQueryResult();
        ret.booleanResult = booleanResult;
        ret.type = Type.BOOLEAN;
        return ret;
    }

    /**
     * The result type: TUPLE, GRAPH or BOOLEAN
     * @return result type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Gets the tuple query result data - a list of variable bindings.
     * @return the list of bindings
     */
    public TupleResult getTupleResult()
    {
        return tupleResult;
    }

    /**
     * Gets the graph query result data - a list of statements
     * @return the list of statements
     */
    public List<Statement> getGraphResult()
    {
        return graphResult;
    }

    /**
     * Gets the boolean query result data - a boolean value
     * @return the boolean value of the result
     */
    public boolean getBooleanResult()
    {
        return booleanResult;
    }

    /**
     * A result of a tuple query consisting of variable names and bindings.
     * 
     * @author burgetr
     */
    public static class TupleResult
    {
        private List<String> variableNames;
        private List<BindingSet> tuples;
        
        public TupleResult(List<String> variableNames, List<BindingSet> tuples)
        {
            this.variableNames = variableNames;
            this.tuples = tuples;
        }

        /**
         * Gets the names of the variables in the result.
         * 
         * @return A list of variable names
         */
        public List<String> getVariableNames()
        {
            return variableNames;
        }

        /**
         * Gets the bindings - the values of the variables in the result.
         * @return A list of binding sets, where each set contains a binding for each variable
         */
        public List<BindingSet> getTuples()
        {
            return tuples;
        }
    }
    
}
