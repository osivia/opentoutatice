/**
 * 
 */
package org.opentoutatice.ecm.scanner.directive.nxql;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.opentoutatice.ecm.scanner.directive.Directive;


/**
 * @author david
 *
 */
public abstract class AbstractNxqlDirective extends Directive {
    
    /** CoreSession. */
    private CoreSession session;

    /**
     * Default constructor.
     */
    public AbstractNxqlDirective() {
        super();
    }

    /**
     * Constructor setting System session (on default repository).
     * 
     * @param query
     */
    public AbstractNxqlDirective(String query) {
        super(query);
        // System CoreSession on defult repository
        this.session = CoreInstance.openCoreSession(null, SecurityConstants.SYSTEM_USERNAME);
    }
    
    public AbstractNxqlDirective(CoreSession session, String query) {
        super(query);
        this.session = session;
    }

    
    /**
     * @return the session
     */
    public CoreSession getSession() {
        return this.session;
    }

    
    /**
     * @param session the session to set
     */
    public void setSession(CoreSession session) {
        this.session = session;
    }

}
