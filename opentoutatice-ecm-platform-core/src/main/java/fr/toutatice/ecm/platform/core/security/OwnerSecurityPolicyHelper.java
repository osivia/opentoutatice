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
    
    /**
     * Get possible aliases of WriteModifyOwnOnly permission.
     * 
     * @return aliases of WriteModifyOwnOnly permission
     */
    public static String[] getAliases(){
    	SecurityService securityService = Framework.getService(SecurityService.class);
    	return securityService.getPermissionProvider().getAliasPermissions(ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR);
    }

    /**
     * Constructor.
     */
    private OwnerSecurityPolicyHelper() {
        super();
    }

}
