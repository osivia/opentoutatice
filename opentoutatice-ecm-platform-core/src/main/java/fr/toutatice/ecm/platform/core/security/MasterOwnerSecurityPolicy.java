/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;
import org.nuxeo.ecm.platform.usermanager.UserService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
public class MasterOwnerSecurityPolicy extends AbstractSecurityPolicy {
    
    private static final Log log = LogFactory.getLog(MasterOwnerSecurityPolicy.class);
    
    /** WorkspaceContainer query. */
    protected static final String WORKSPACE_QUERY = "select * from Workspace where ecm:uuid = '%s'"
            + " and ecm:isProxy = 0 and ecm:currentLifeCycleState <> 'deleted' and ecm:isVersion = 0";

    /** User service. */
    protected final UserService userService = Framework.getService(UserService.class);

   /**
    * Grant if principal has MasterOwner permission at Workspace root level.
    */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        
        try {
            // FIXME: better to use Session at this level
//            Principal systemPrincipal = new NuxeoPrincipalImpl(SecurityConstants.SYSTEM_USERNAME);
//            QueryFilter systemFilter = new QueryFilter(systemPrincipal, null, null, null, null, 1, 1);
//            String query = String.format(WORKSPACE_QUERY, doc.getSystemProp("ancestors", String[].class));
            
            // Get Workspace
            CoreSession sessionSystem = null;
            try {
            sessionSystem = CoreInstance.openCoreSessionSystem(doc.getRepositoryName());
            DocumentModel documentModel = sessionSystem.getDocument(new IdRef(doc.getUUID()));
            DocumentModelList spaces = ToutaticeDocumentHelper.getParentSpaceList(sessionSystem, documentModel, true, true);
            
            if(spaces != null && spaces.size() == 1){
                DocumentModel space = spaces.get(0);
                if("Workspace".equals(space.getType())){
                    // Checks if current user has MasterOwner permission
                    ACP wsAcp = space.getACP();
                    if(!ArrayUtils.contains(additionalPrincipals, principal.getName())){
                        additionalPrincipals[additionalPrincipals.length] = principal.getName();
                    }
                    
                    boolean hasAllPermissions = Access.GRANT.equals(wsAcp.getAccess(additionalPrincipals,
                            new String[]{SecurityConstants.EVERYTHING}));
        
                    if(!hasAllPermissions && Access.GRANT.equals(wsAcp.getAccess(additionalPrincipals,
                            new String[]{ToutaticeNuxeoStudioConst.CST_PERM_MASTER_OWNER}))){
                        return Access.GRANT;
                    }
                }
            }
            } finally {
                if(sessionSystem != null){
                    sessionSystem.close();
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        
        return Access.UNKNOWN;
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
