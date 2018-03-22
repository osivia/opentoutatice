/**
 * 
 */
package fr.toutatice.ecm.platform.web.filemanager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.webapp.filemanager.FileManageActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
@Name("FileManageActions")
@Scope(ScopeType.EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeFileManageActionsBean extends FileManageActionsBean {
    
    /**
     * Override to disable remote proxies deplacement
     * to keep consistent references.
     */
    @Override
    @WebRemote
    protected String checkMoveAllowed(DocumentRef docRef, DocumentRef containerRef) throws NuxeoException {

        DocumentModel doc = documentManager.getDocument(docRef);
        
        String status = super.checkMoveAllowed(docRef, containerRef);
        
        // To avoid republication of local proxies (cf FileManageActionsBean#moveWithId): 
        // move is sufficient 
        if(MOVE_PUBLISH.equals(status)){
            DocumentModel localProxy = ToutaticeDocumentHelper.getProxy(documentManager, doc, null);
            if(localProxy != null){
                status = MOVE_OK;
            }
        }
        
        return status;
    }
    
    /*
     * FIXME: can we find a way to override without redefine all mthods?
     */
    @Override
    @WebRemote
    public String addFolderFromPlugin(String fullName, String morePath) throws NuxeoException{
        return super.addFolderFromPlugin(fullName, morePath);
    }
    
    @Override
    @WebRemote
    public String moveWithId(String docId, String containerId) throws NuxeoException {
        return super.moveWithId(docId, containerId);
    }
    
    @Override
    @WebRemote
    public String copyWithId(String docId) throws NuxeoException {
        return super.copyWithId(docId);
    }

    @WebRemote
    public String pasteWithId(String docId) throws NuxeoException {
        return super.pasteWithId(docId);
    }
    
    @Override
    @WebRemote
    public String removeUploadedFile(String fileName) throws NuxeoException {
        return super.removeUploadedFile(fileName);
    }
    
    @Override
    @WebRemote
    public String removeAllUploadedFile() throws NuxeoException {
        return super.removeAllUploadedFile();
    }
    
    @Override
    @WebRemote
    public String removeSingleUploadedFile() throws NuxeoException {
        return removeSingleUploadedFile();
    }


}
