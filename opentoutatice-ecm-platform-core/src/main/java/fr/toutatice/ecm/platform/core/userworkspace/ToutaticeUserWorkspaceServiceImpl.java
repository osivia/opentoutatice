/**
 * 
 */
package fr.toutatice.ecm.platform.core.userworkspace;

import java.security.Principal;

import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.SystemPrincipal;
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

        CoreSession userSession = null;
        try {

            NuxeoPrincipal principalCaller = (NuxeoPrincipal) session.getPrincipal();

            // Case when this method is called in unrestricted session (like via UserProfileService)
            if (principalCaller instanceof SystemPrincipal) {
                userSession = CoreInstance.openCoreSession(null, username);
            }

            // If it is username workspace, user has Everything permission on it
            if (!new UnrestrictedPermissionChecker(userSession, uwref).hasPermission()) {
                int digestLength = workspacename.length() / 3;
                // Generate new path
                String substring = workspacename.substring(0, workspacename.length() - digestLength);
                return new PathRef(rootref, substring.concat(digest(username, digestLength)));
            }

        } finally {
            if (userSession != null) {
                userSession.close();
            }
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
            this.principal = session.getPrincipal();
        }

        @Override
        public void run() {
            this.hasPermission = !this.session.exists(this.ref) || this.session.hasPermission(this.principal, this.ref, SecurityConstants.EVERYTHING);
        }

        /**
         * @return true if user has permission on document.
         */
        boolean hasPermission() {
            runUnrestricted();
            return this.hasPermission;
        }
    }

}
