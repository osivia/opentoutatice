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
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.webapp.filemanager.FileManageActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


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
    protected String checkMoveAllowed(DocumentRef docRef, DocumentRef containerRef) throws ClientException {

        DocumentModel doc = documentManager.getDocument(docRef);
        if(doc.isProxy() && doc.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)){
            facesMessages.add(StatusMessage.Severity.WARN, messages.get("move_impossible"));
            return MOVE_IMPOSSIBLE;
        }
        
        return super.checkMoveAllowed(docRef, containerRef);
    }
    
    /*
     * FIXME: can we find a way to override without redefine all mthods?
     */
    @Override
    @WebRemote
    public String addFolderFromPlugin(String fullName, String morePath) throws ClientException{
        return super.addFolderFromPlugin(fullName, morePath);
    }
    
    @Override
    @WebRemote
    public String moveWithId(String docId, String containerId) throws ClientException {
        return super.moveWithId(docId, containerId);
    }
    
    @Override
    @WebRemote
    public String copyWithId(String docId) throws ClientException {
        return super.copyWithId(docId);
    }

    @WebRemote
    public String pasteWithId(String docId) throws ClientException {
        return super.pasteWithId(docId);
    }
    
    @Override
    @WebRemote
    public String removeUploadedFile(String fileName) throws ClientException {
        return super.removeUploadedFile(fileName);
    }
    
    @Override
    @WebRemote
    public String removeAllUploadedFile() throws ClientException {
        return super.removeAllUploadedFile();
    }
    
    @Override
    @WebRemote
    public String removeSingleUploadedFile() throws ClientException {
        return removeSingleUploadedFile();
    }


}
