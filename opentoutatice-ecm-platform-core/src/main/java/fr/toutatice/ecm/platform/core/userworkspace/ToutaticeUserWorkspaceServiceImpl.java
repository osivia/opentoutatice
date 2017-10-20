/**
 * 
 */
package fr.toutatice.ecm.platform.core.userworkspace;

import java.security.Principal;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.userworkspace.core.service.DefaultUserWorkspaceServiceImpl;


/**
 * Used to to manage namsake user workspace names in better way.
 * 
 * @author david
 *
 */
public class ToutaticeUserWorkspaceServiceImpl extends DefaultUserWorkspaceServiceImpl {

    private static final long serialVersionUID = 4769849621676186385L;

    @Override
    protected PathRef resolveUserWorkspace(CoreSession session, PathRef rootref, String username, String workspacename, int maxsize) {
        PathRef uwref = new PathRef(rootref, workspacename);
        // If it is username workspace, user has Everything permission on it
        if (!new UnrestrictedPermissionChecker(session, uwref).hasPermission()) {
            int digestLength = workspacename.length() / 3;
            // Generate new path
            String substring = workspacename.substring(0, workspacename.length() - digestLength);
            return new PathRef(rootref, substring.concat(digest(username, digestLength)));
        }
        return uwref;
    }

    /**
     * Checks if user has permission on given document.
     */
    protected class UnrestrictedPermissionChecker extends UnrestrictedSessionRunner {

        /** User. */
        final Principal principal;
        /** Document. */
        final PathRef ref;
        /** Result. */
        boolean hasPermission;

        protected UnrestrictedPermissionChecker(CoreSession session, PathRef ref) {
            super(session);
            this.ref = ref;
            principal = session.getPrincipal();
        }

        @Override
        public void run() {
            hasPermission = !session.exists(ref) || session.hasPermission(principal, ref, SecurityConstants.EVERYTHING);
        }

        /**
         * @return true if user has permission on document.
         */
        boolean hasPermission() {
            runUnrestricted();
            return hasPermission;
        }
    }

}
