/**
 * 
 */
package fr.toutatice.ecm.platform.service.security;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;


/**
 * @author david
 *
 */
public interface DocumentSecurityInformationsProvider extends DocumentInformationsProvider {
    
    /**
     *  Actions to test.
     */
    enum TestedActions {
        canCopy;
    }
    
    /**
     * Checks if document can be copied in its parent Folder.
     * 
     * @param coreSession
     * @param currentDocument
     * @return true if document can be copied
     */
    boolean canCopy(CoreSession coreSession, DocumentModel currentDocument);
    
    /**
     * Checks if document can be copied in target Folder.
     * 
     * @param coreSession
     * @param currentDocument
     * @param target
     * @return true if document can be copied
     */
    boolean canCopyTo(CoreSession coreSession, DocumentModel currentDocument, DocumentModel target);
    
}
