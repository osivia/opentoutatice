package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.StringUtils;
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
	
	/**
	 * {@inheritDoc}
	 */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        Access access = Access.UNKNOWN;

        try {
            if (doPolicyApply(mergedAcp, principal, additionalPrincipals)) {
                if (doc.isFolder()) {
                    access = applyPolicyToFolderish(doc, principal, permission, resolvedPermissions);
                } else {
                    access = applyPolicyToLeaf(doc, principal, permission, resolvedPermissions);
                }
            }
        } catch (DocumentException e) {
            log.error("Failed to evaluate the policy, error: " + e.getMessage());
        }

        return access;
    }
	
	
	/**
	 * Policy is applied if principal has WriteModifyOwnOnly permission.
	 * 
	 * @param mergedAcp
	 * @param principal
	 * @param additionalPrincipals
	 * @return true if principal has WriteModifyOwnOnly
	 */
    private boolean doPolicyApply(ACP mergedAcp, Principal principal, String[] additionalPrincipals) {

        List<String> principalsList = new ArrayList<String>();
        if (null != additionalPrincipals) {
            principalsList.addAll(Arrays.asList(additionalPrincipals));
        } else {
            principalsList.add(principal.getName());
        }

        return Access.GRANT.equals(mergedAcp.getAccess(principalsList.toArray(new String[principalsList.size()]),
                new String[]{ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR}));

    }
	
	/**
     * Store Folderish id for which allowed sub types filtered {@link OwnerSecurityPolicyHelper}.
     * 
     * @param doc
     * @param principal
     * @throws DocumentException
     */
    protected Access applyPolicyToFolderish(Document doc, Principal principal, String permission,
            String[] resolvedPermissions) throws DocumentException {
        
        // Can read, create, delete, move, import and copy (non Folderish documents)
        if(SecurityConstants.READ.equals(permission) || SecurityConstants.ADD_CHILDREN.equals(permission) 
                || (SecurityConstants.REMOVE_CHILDREN.equals(permission))){
            currentAppliedPolicyFolderishId = doc.getUUID();
            return Access.GRANT;
        }
        // Continue SesurityServiceImpl#checkPermission 
        return Access.UNKNOWN;
        
    }

    /**
     * Only creator can update, delete document.
     * 
     * @param doc
     * @param principal
     * @return  grant if principal is document's creator
     * @throws DocumentException
     */
    protected Access applyPolicyToLeaf(Document doc, Principal principal, String permission,
            String[] resolvedPermissions) throws DocumentException {
        
        if(isCreator(doc, principal)){
            // Can read, update, move and remove its documents (leafs)
            if(SecurityConstants.READ.equals(permission) || (SecurityConstants.WRITE_PROPERTIES.equals(permission))
                    || (SecurityConstants.REMOVE.equals(permission)) || (SecurityConstants.WRITE_VERSION.equals(permission))
                    // Group Permission (TODO in another way (get SubGroupPermission)
                    || (SecurityConstants.WRITE.equals(permission))){
                return Access.GRANT;
            }
        }
        // Continue SesurityServiceImpl#checkPermission
        return Access.UNKNOWN;
        
    }
    
    /**
     * Checks if principal is creator of document.
     * 
     * @param doc
     * @param principal
     * @return true if principal is creator of document
     * @throws DocumentException 
     */
    private boolean isCreator(Document doc, Principal principal) throws DocumentException{
        String creator = (String) doc.getPropertyValue("dc:creator");
        return StringUtils.equals(principal.getName(), creator);
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
