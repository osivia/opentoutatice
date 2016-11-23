package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
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
	
	/** Permissions to check on document. */
	protected static final String[] DOCUMENT_PERMISSIONS = {SecurityConstants.READ, SecurityConstants.WRITE};
	/** Permissions to check on document's parent. */
	protected static final String[] PARENT_PERMISSIONS = {SecurityConstants.READ, SecurityConstants.REMOVE_CHILDREN};
	/** Permissions to check on Folderish document. */
	protected static final String[] FOLDERISH_PERMISSIONS = {SecurityConstants.ADD_CHILDREN};
	
	/** Current Folderish id on which policy applying. */
	private static String currentOwnerPoliciedFolderishId;
	
	/**
	 * Gets current Folderish id on which policy applying.
	 * 
	 * @return current Folderish id on which policy applying
	 */
	public static synchronized String getCurrentOwnerPoliciedFolderishId(){
	    return currentOwnerPoliciedFolderishId;
	}
	
	/**
	 * Resets current Folderish id on which policy applying.
	 */
	public static synchronized void resetCurrentOwnerPoliciedFolderishId(){
	    currentOwnerPoliciedFolderishId = null;
	}

    /**
	 * {@inheritDoc}
	 */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        Access access = Access.UNKNOWN;

        try {
            if (doPolicyApply(permission, mergedAcp, principal, additionalPrincipals)) {
                if (doc.isFolder()) {
                    access = applyPolicyToFolderish(doc, principal, permission);
                } else {
                    access = applyPolicyToLeaf(doc, principal, resolvedPermissions);
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
	 * @param permission
	 * @param mergedAcp
	 * @param principal
	 * @param additionalPrincipals
	 * @return true if principal has WriteModifyOwnOnly
	 */
    private boolean doPolicyApply(String permission, ACP mergedAcp, Principal principal, String[] additionalPrincipals) {
        
        if(!ArrayUtils.contains(additionalPrincipals, principal.getName())){
            additionalPrincipals[additionalPrincipals.length] = principal.getName();
        }
        
        // Don't apply policy when user/groups has Everything permission
        // (we must test it cause getAccess always return true if user has Everything permission
        // neverless the perm argument like ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR)
        boolean hasAllPermissions = Access.GRANT.equals(mergedAcp.getAccess(additionalPrincipals,
                new String[]{SecurityConstants.EVERYTHING}));

        return !hasAllPermissions && Access.GRANT.equals(mergedAcp.getAccess(additionalPrincipals,
                new String[]{ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR}));

    }
	
	/**
     * Store Folderish id for which allowed sub types filtered {@link OwnerSecurityPolicyHelper}.
     * 
     * @param doc
     * @param principal
     * @throws DocumentException
     */
    protected Access applyPolicyToFolderish(Document doc, Principal principal, String permission) throws DocumentException {
        
        // Can read, create, delete, move, import and copy (non Folderish documents)
        if(ArrayUtils.contains(FOLDERISH_PERMISSIONS, permission) || ArrayUtils.contains(PARENT_PERMISSIONS, permission)) {
            // add (and copy...)
            if(ArrayUtils.contains(FOLDERISH_PERMISSIONS, permission)) {
                currentOwnerPoliciedFolderishId = doc.getUUID();
            }
            
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
    protected Access applyPolicyToLeaf(Document doc, Principal principal, String[] resolvedPermissions) throws DocumentException {
        
        if(isCreator(doc, principal)){
            // Contributor can read, update, move and remove its documents (leafs)
            String[] allowedPerms =  org.nuxeo.common.utils.ArrayUtils.intersect(DOCUMENT_PERMISSIONS, resolvedPermissions);
            
            if(ArrayUtils.isNotEmpty(allowedPerms)){
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
