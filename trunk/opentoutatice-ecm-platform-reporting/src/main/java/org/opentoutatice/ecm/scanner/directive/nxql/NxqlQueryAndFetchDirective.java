/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive.nxql;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.query.sql.NXQL;


/**
 * @author david
 *
 */
public class NxqlQueryAndFetchDirective extends AbstractNxqlDirective {
    
    /**
     * Default constructor.
     */
    public NxqlQueryAndFetchDirective() {
        super();
    }
    
    /**
     * Constructor setting System session (on default repository).
     * 
     * @param query
     */
    public NxqlQueryAndFetchDirective(String query) {
        super(query);
    }
    
    /**
     * @param session
     * @param query
     */
    public NxqlQueryAndFetchDirective(CoreSession session, String query){
        super(session, query);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IterableQueryResult execute() throws Exception {
        IterableQueryResult queryResult = getSession().queryAndFetch(getQuery(), NXQL.NXQL, new Object[0]);
        // To clear caches ??
        getSession().save();
        
        return queryResult;
    }
    
}
