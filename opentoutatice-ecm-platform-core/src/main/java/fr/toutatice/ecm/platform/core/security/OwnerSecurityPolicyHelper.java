/**
 * 
 */
package fr.toutatice.ecm.platform.core.security;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class OwnerSecurityPolicyHelper {
    
    /** UI Type Manager. */
    private static final TypeManager uiTypeManager = Framework.getService(TypeManager.class);
    /** Folderish types list. */
    private static Set<String> folderishTypes;
    
    /**
     * Get list of DataModel Folderish types.
     */
    //FIXME: Hot Reload?
    private static Set<String> getFolderishTypes() {
        if(folderishTypes == null){
            SchemaManager schemaManager = Framework.getService(SchemaManager.class);
            folderishTypes = schemaManager.getDocumentTypeNamesForFacet("Folderish");
        }
        return folderishTypes;
    }

    /**
     * Constructor.
     */
    private OwnerSecurityPolicyHelper() {
        super();
    }
    
    /**
     * 
     * @param document
     * @return
     */
    public static Collection<Type> getFilteredAllowedSubTypes(DocumentModel document, Principal principal) {
        Collection<Type> leafSubTypes = new HashSet<Type>(0);

        try {
            Collection<Type> allowedSubTypes = uiTypeManager.getAllowedSubTypes(document.getType(), document);

            String folderishId = ToutaticeOwnerSecurityPolicy.getCurrentOwnerPoliciedFolderishId();
            if (StringUtils.equals(document.getId(), folderishId)) {

                Set<String> folderish = getFolderishTypes();

                if (folderish != null && allowedSubTypes != null) {
                    for (Type type : allowedSubTypes) {
                        if (!folderish.contains(type.getId())) {
                            leafSubTypes.add(type);
                        }
                    }
                }
            } else {
                leafSubTypes.addAll(allowedSubTypes);
            }
        } finally {
            ToutaticeOwnerSecurityPolicy.resetCurrentOwnerPoliciedFolderishId();
        }

        return leafSubTypes;
    }
    
    /**
     * Checks if principal is application or functionnal admin.
     * 
     * @param document
     * @param principal
     * @return true if principal is admin
     */
    private static boolean isAdmin(DocumentModel document, Principal principal){
        if(((NuxeoPrincipal) principal).isAdministrator()){
            return true;
        }
        
        ACP acp = document.getACP();
        Access access = acp.getAccess(principal.getName(), SecurityConstants.EVERYTHING);
        return Access.GRANT.equals(access);
    }

}
