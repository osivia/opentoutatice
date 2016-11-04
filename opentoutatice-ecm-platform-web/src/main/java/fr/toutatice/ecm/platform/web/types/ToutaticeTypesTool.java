/**
 * 
 */
package fr.toutatice.ecm.platform.web.types;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.webapp.action.TypesTool;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.security.OwnerSecurityPolicyHelper;


/**
 * @author david
 *
 */
@Name("typesTool")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeTypesTool extends TypesTool {

    private static final long serialVersionUID = 7573925069509154652L;
    
    /** Key for owners of document (stored in context). */
    public static final String DOC_OWNERS_KEYS = "owners";
    
    /**
     * 
     */
    @Override
    protected Map<String, List<Type>> filterTypeMap(Map<String, List<Type>> docTypeMap) {
        DocumentModel currentDocument = super.getConfigurationDocument();
        if (currentDocument != null) {
            Principal principal = currentDocument.getCoreSession().getPrincipal();
            
            Collection<Type> filteredAllowedSubTypes = OwnerSecurityPolicyHelper.getFilteredAllowedSubTypes(currentDocument, principal);
            if (CollectionUtils.isNotEmpty(filteredAllowedSubTypes)) {

                Map<String, List<Type>> filteredAllowedSubTypesMap = new HashMap<String, List<Type>>(1);
                List<Type> filteredAllowedSubTypesList = new ArrayList<Type>(filteredAllowedSubTypes);
                filteredAllowedSubTypesMap.put(currentDocument.getType(), filteredAllowedSubTypesList);
                return filteredAllowedSubTypesMap;

            }
        }

        return docTypeMap;
    }

}
