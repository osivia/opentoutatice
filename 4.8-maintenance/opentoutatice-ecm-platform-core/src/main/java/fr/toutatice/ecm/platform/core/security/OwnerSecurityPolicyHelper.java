/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import org.nuxeo.ecm.core.security.SecurityService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 *
 */
public class OwnerSecurityPolicyHelper {
    
    /** SecurityService. */
    private static SecurityService securitySrv;
    
    /**
     * Getter for SecurityService.
     */
    public static SecurityService getSecurityService(){
        if(securitySrv == null){
            securitySrv = Framework.getService(SecurityService.class);
        }
        return securitySrv;
    }

    /**
     * Get possible aliases of WriteModifyOwnOnly permission.
     * 
     * @return aliases of WriteModifyOwnOnly permission
     */
    public static String[] getAliases(){
    	return getSecurityService().getPermissionProvider().getAliasPermissions(ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR);
    }
    
    /**
     * Constructor.
     */
    private OwnerSecurityPolicyHelper() {
        super();
    }

}
