/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive;


/**
 * @author david
 *
 */
public abstract class Directive {

    /** Directive's query. */
    private String query;

    /**
     * Default constructor.
     */
    public Directive() {
        super();
    }

    /**
     * @param query
     */
    public Directive(String query) {
        this.query = query;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Executes a directive.
     * 
     * @return DirectiveResults
     * @throws Exception
     */
    public abstract Iterable<?> execute() throws Exception;

}
