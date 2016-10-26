/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;
import org.nuxeo.ecm.platform.usermanager.UserService;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class OwnerMemberSecurityPolicy extends AbstractSecurityPolicy {
    
    /** Default owner group. */
    protected static final String OWNER_GROUP = "Owners";

    /** User service. */
    protected final UserService userService = Framework.getService(UserService.class);

   /**
    * Grant if principal is member of Owner group.
    */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        Access access = Access.UNKNOWN;
        
        String ownerGroup = Framework.getProperty("ottc.owner.group", OWNER_GROUP);
        if(StringUtils.isNotBlank(ownerGroup)){
            List<String> allPrincipalGroups = ((NuxeoPrincipal) principal).getAllGroups();
            if(allPrincipalGroups != null && allPrincipalGroups.contains(ownerGroup)){
                access = Access.GRANT;
            }
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
