/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 *
 */
public class MasterOwnerSecurityPolicy extends AbstractSecurityPolicy {

    private static final Log log = LogFactory.getLog(MasterOwnerSecurityPolicy.class);

    /**
     * If principal (or one of its groups) has MasterOwner permission, he has all rights (MatserOwner permission simulates Everything permission).
     */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        // Trace logs
        long begin = System.currentTimeMillis();

        // Check
        Access access = mergedAcp.getAccess(additionalPrincipals, new String[]{ToutaticeNuxeoStudioConst.CST_PERM_MASTER_OWNER});
        if (!Access.GRANT.equals(access)) {
            access = Access.UNKNOWN;
        }


        if (log.isTraceEnabled()) {
            long end = System.currentTimeMillis();
            log.trace(": " + String.valueOf(end - begin) + " ms");
        }

        return access;
    }

    @Override
    public boolean isRestrictingPermission(String permission) {
        assert permission.equals("Browse"); // others not coded
        return false;
    }

    @Override
    public boolean isExpressibleInQuery() {
        return true;
    }

    @Override
    public SQLQuery.Transformer getQueryTransformer() {
        return SQLQuery.Transformer.IDENTITY;
    }

}
