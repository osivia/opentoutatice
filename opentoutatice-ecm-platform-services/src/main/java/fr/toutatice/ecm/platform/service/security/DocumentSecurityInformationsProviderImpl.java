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
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class DocumentSecurityInformationsProviderImpl implements DocumentSecurityInformationsProvider {

    /** Types manager. */
    private static TypeManager typeMgr = null;

    /**
     * Getter for TypeManager.
     */
    public static TypeManager getTypeManager() {
        if (typeMgr == null) {
            typeMgr = (TypeManager) Framework.getService(TypeManager.class);
        }
        return typeMgr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDoc) throws ClientException {
        Map<String, Object> securityInfo = new HashMap<String, Object>();
        securityInfo.put(canCopy.name(), canCopy(coreSession, currentDoc));
        return securityInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCopy(CoreSession coreSession, DocumentModel currentDoc) {
        boolean canCopy = false;

        DocumentRef parentRef = coreSession.getParentDocumentRef(currentDoc.getRef());

        if (parentRef != null) {
            boolean isAdmin = coreSession.hasPermission(currentDoc.getRef(), SecurityConstants.EVERYTHING);
            if (isAdmin) {
                return true;
            }

            boolean canAdd = coreSession.hasPermission(parentRef, SecurityConstants.ADD_CHILDREN);
            if (canAdd) {
                if (currentDoc.isFolder()) {
                    Collection<Type> subTypes = getTypeManager().getAllowedSubTypes(currentDoc.getType(), currentDoc);
                    canCopy = CollectionUtils.isNotEmpty(subTypes);
                } else {
                    canCopy = true;
                }
            }
        }

        return canCopy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canCopyTo(CoreSession coreSession, DocumentModel currentDocument, DocumentModel target) {
        return false;
    }

}
