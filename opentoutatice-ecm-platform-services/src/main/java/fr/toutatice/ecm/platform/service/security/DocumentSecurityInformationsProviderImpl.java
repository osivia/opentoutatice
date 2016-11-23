/**
 * 
 */
package fr.toutatice.ecm.platform.service.security;

import static fr.toutatice.ecm.platform.service.security.DocumentSecurityInformationsProvider.TestedActions.canCopy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.types.Type;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.security.OwnerSecurityPolicyHelper;


/**
 * @author david
 *
 */
public class DocumentSecurityInformationsProviderImpl implements DocumentSecurityInformationsProvider {
    
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        
        Map<String, Object> securityInfo = new HashMap<String, Object>();
        
        securityInfo.put(canCopy.name(), canCopy(coreSession, currentDocument));
        
        return securityInfo;
    }

    @Override
    public boolean canCopy(CoreSession coreSession, DocumentModel currentDocument) {
        boolean canCopy = false;

        DocumentRef parentRef = coreSession.getParentDocumentRef(currentDocument.getRef());

        boolean isAdmin = coreSession.hasPermission(currentDocument.getRef(), SecurityConstants.EVERYTHING);
        if (isAdmin) {
            return true;
        }

        boolean canAdd = coreSession.hasPermission(parentRef, SecurityConstants.ADD_CHILDREN);
        if (canAdd) {
            // Owner policy (to externalize in inherited class?)
            boolean isUserOwner = coreSession.hasPermission(currentDocument.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_CONTRIBUTOR);
            if (isUserOwner) {
                // Owner can not copy Folderish
                canCopy = !currentDocument.isFolder();
            } else {
                if (currentDocument.isFolder()) {
                    Collection<Type> subTypes = OwnerSecurityPolicyHelper.getFilteredAllowedSubTypes(currentDocument, coreSession.getPrincipal());
                    canCopy = CollectionUtils.isNotEmpty(subTypes);
                } else {
                    canCopy = true;
                }
            }
        }

        return canCopy;
    }

    @Override
    public boolean canCopyTo(CoreSession coreSession, DocumentModel currentDocument, DocumentModel target) {
        // TODO Auto-generated method stub
        return false;
    }

}
