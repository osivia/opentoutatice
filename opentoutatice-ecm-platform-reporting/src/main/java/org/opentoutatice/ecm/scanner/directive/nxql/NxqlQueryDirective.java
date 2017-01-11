/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive.nxql;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author david
 *
 */
public class NxqlQueryDirective extends AbstractNxqlDirective {

    /**
     * Default constructor.
     */
    public NxqlQueryDirective() {
        super();
    }

    /**
     * Constructor setting System session (on default repository).
     * 
     * @param query
     */
    public NxqlQueryDirective(String query) {
        super(query);
    }

    /**
     * @param session
     * @param query
     */
    public NxqlQueryDirective(CoreSession session, String query) {
        super(session, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentModelList execute() throws Exception {
        // FIXME: take DocumentPageProvider into account ??
        DocumentModelList modelList = getSession().query(getQuery());
        // To clear query caches ??
        getSession().save();
        
        return modelList;
    }

}
