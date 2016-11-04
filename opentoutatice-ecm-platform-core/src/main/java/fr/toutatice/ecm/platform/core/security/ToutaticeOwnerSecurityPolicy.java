package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.ecm.core.security.AbstractSecurityPolicy;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

public class ToutaticeOwnerSecurityPolicy extends AbstractSecurityPolicy {

	private static final Log log = LogFactory.getLog(ToutaticeOwnerSecurityPolicy.class);
	
	/** Current Folderish id on which policy applying. */
	private static String currentAppliedPolicyFolderishId;
	
	/**
	 * Gets current Folderish id on which policy applying.
	 * 
	 * @return current Folderish id on which policy applying
	 */
	public static String getCurrentAppliedPolicyFolderishId(){
	    return currentAppliedPolicyFolderishId;
	}
	
	/**
	 * Resets current Folderish id on which policy applying.
	 */
	public static void resetCurrentAppliedFolderishId(){
	    currentAppliedPolicyFolderishId = null;
	}

	@Override
	public Access checkPermission(Document doc, 
			ACP mergedAcp,
			Principal principal, 
			String permission,
			String[] resolvedPermissions, 
			String[] additionalPrincipals) {
		Access access = Access.UNKNOWN;

		try {
			if (doPolicyApply(doc, mergedAcp, principal, permission, additionalPrincipals)) {
			    if(doc.isFolder()){
			        access = applyPolicyToFolderish(doc, principal);
			    } else {
			        access = applyPolicyToLeaf(doc, principal);
			    }
			}
		} catch (DocumentException e) {
			log.error("Failed to evaluate the policy, error: " + e.getMessage());
		}

		return access;
	}
	
	
	/**
	 * 
	 * 
	 * @param doc
	 * @param mergedAcp
	 * @param principal
	 * @param permission
	 * @param additionalPrincipals
	 * @return true if WriteModifyOwnOnly policy can be applied.
	 */
	private boolean doPolicyApply(Document doc, ACP mergedAcp, Principal principal, String permission, String[] additionalPrincipals) {
		boolean status = false;
		
		boolean isWritePermission = SecurityConstants.WRITE_PROPERTIES.equals(permission) || SecurityConstants.WRITE.equals(permission) || SecurityConstants.REMOVE.equals(permission)
                 || SecurityConstants.WRITE_VERSION.equals(permission);
		boolean isSetOnLinePermission = ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE.equals(permission);
		
		if (isWritePermission || isSetOnLinePermission) {
			List<String> principalsList = new ArrayList<String>();
			if (null != additionalPrincipals) {
				principalsList.addAll(Arrays.asList(additionalPrincipals));
			} else {
				principalsList.add(principal.getName());
			}
			
			Access acessContributor = mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]), new String[] {ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR});
			
			if (Access.GRANT == acessContributor) {
                if ((isWritePermission && Access.GRANT != getWriteAccess(mergedAcp, principalsList))
                        || (isSetOnLinePermission && Access.GRANT != getOnLineAccess(mergedAcp, principalsList))) {
                    status = true;
                }
            }
		}

		return status;
	}
	
	/**
     * Store Folderish id for which allowed sub types filtered {@link OwnerSecurityPolicyHelper}.
     * 
     * @param doc
     * @param principal
     * @throws DocumentException
     */
    protected Access applyPolicyToFolderish(Document doc, Principal principal) throws DocumentException {
        currentAppliedPolicyFolderishId = doc.getUUID();
        return Access.DENY;
    }

    /**
     * Only creator can update, delete document.
     * 
     * @param doc
     * @param principal
     * @return  grant if principal is document's creator
     * @throws DocumentException
     */
    protected Access applyPolicyToLeaf(Document doc, Principal principal) throws DocumentException {
        Access access;
        String creator = (String) doc.getPropertyValue("dc:creator");
        if (!principal.getName().equals(creator)) {
            access = Access.DENY;
        } else {
            access = Access.GRANT;
        }
        return access;
    }
	
	/**
     * @param mergedAcp
     * @param principalsList
     * @return
     */
    private Access getOnLineAccess(ACP mergedAcp, List<String> principalsList) {
        return mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]),
                new String[]{ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE});
    }

    /**
     * @param mergedAcp
     * @param principalsList
     * @return
     */
    private Access getWriteAccess(ACP mergedAcp, List<String> principalsList) {
        return mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]), new String[]{SecurityConstants.READ_WRITE});
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
