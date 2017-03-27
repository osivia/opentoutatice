package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;

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

    /** Permissions to simulate on document to allow actions. */
    private static final String[] SIMULATED_DOCUMENT_PERMISSIONS = {SecurityConstants.READ, SecurityConstants.WRITE,
            ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE};
    /** Permissions to simulate on parent's document to allow actions. */
    private static final String[] SIMULATED_PARENT_PERMISSIONS = {SecurityConstants.READ, ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE};
    /** Permissions to simulate on parent's document to allow actions but not conditioned by the fact that current user must be creator of parent's document. */
    private static final String[] NOT_CONDITIONED_SIMULATED_PARENT_PERMISSIONS = {SecurityConstants.READ, SecurityConstants.ADD_CHILDREN,
            SecurityConstants.REMOVE_CHILDREN};

    /**
     * Getter for document permissions to simulate.
     */
    protected String[] getSimulatedDocumentPermissions() {
        return SIMULATED_DOCUMENT_PERMISSIONS;
    }

    /**
     * Getter for parent permissions to simulate.
     */
    protected String[] getSimulatedParentPermissions() {
        return SIMULATED_PARENT_PERMISSIONS;
    }

    /**
     * Getter for parents permissions (not conditioned on creator) to simulate.
     */
    protected String[] getNotConditionedParentPermissions() {
        return NOT_CONDITIONED_SIMULATED_PARENT_PERMISSIONS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, String permission, String[] resolvedPermissions,
            String[] additionalPrincipals) {
        Access access = Access.UNKNOWN;

        // Owner permission and its possible aliases
        String[] ownerPermissions = (String[]) ArrayUtils.add(OwnerSecurityPolicyHelper.getAliases(), ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR);

        try {
            if (doPolicyApply(ownerPermissions, mergedAcp, principal, additionalPrincipals)) {
                access = applyPolicy(doc, principal, resolvedPermissions);
            }
        } catch (DocumentException e) {
            log.error("Failed to evaluate the policy, error: " + e.getMessage());
        }

        return access;
    }


    /**
     * Policy is applied if principal has WriteModifyOwnOnly permission.
     * 
     * @param ownerPermissions
     * @param mergedAcp
     * @param principal
     * @param additionalPrincipals
     * @return true if principal has WriteModifyOwnOnly
     */
    private boolean doPolicyApply(String[] ownerPermissions, ACP mergedAcp, Principal principal, String[] additionalPrincipals) {

        if (!ArrayUtils.contains(additionalPrincipals, principal.getName())) {
            additionalPrincipals[additionalPrincipals.length] = principal.getName();
        }

        // Don't apply policy when user/groups has Everything permission
        // (we must test it cause getAccess always return true if user has Everything permission
        // nevertheless the perm argument like ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR)
        boolean hasEverythingPerm = Access.GRANT.equals(mergedAcp.getAccess(additionalPrincipals, new String[]{SecurityConstants.EVERYTHING}));

        return !hasEverythingPerm && Access.GRANT.equals(mergedAcp.getAccess(additionalPrincipals, ownerPermissions));

    }

    /**
     * Only creator can update, delete document.
     * 
     * @param doc
     * @param principal
     * @return grant if principal is document's creator
     * @throws DocumentException
     */
    protected Access applyPolicy(Document doc, Principal principal, String[] resolvedPermissions) throws DocumentException {
        // Permissions to simulate
        String[] simulatedPerms = null;

        if (isCreator(doc, principal)) {
            // Contributor can update, remove and move and its documents
            simulatedPerms = (String[]) ArrayUtils.addAll(getSimulatedDocumentPermissions(), getSimulatedParentPermissions());
        } else {
            // He can add and import documents in Folder he hasn't created (not his)
            // he can also copy not his
            simulatedPerms = getNotConditionedParentPermissions();
        }

        String[] allowedPerms = org.nuxeo.common.utils.ArrayUtils.intersect(simulatedPerms, resolvedPermissions);

        if (ArrayUtils.isNotEmpty(allowedPerms)) {
            return Access.GRANT;
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
    protected boolean isCreator(Document doc, Principal principal) throws DocumentException {
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
