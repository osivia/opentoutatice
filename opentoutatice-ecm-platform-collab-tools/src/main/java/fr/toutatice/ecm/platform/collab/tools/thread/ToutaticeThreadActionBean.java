package fr.toutatice.ecm.platform.collab.tools.thread;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.forum.web.ThreadActionBean;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;

@Name("threadAction")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeThreadActionBean extends ThreadActionBean {

	private static final long serialVersionUID = 1L;

	@In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected EventManager eventManager;

	@Override
	protected DocumentModel getThreadModel() throws ClientException {
		DocumentModel currentChangeableDocument = navigationContext.getChangeableDocument();
		this.title = currentChangeableDocument.getTitle();
		this.description = (String) currentChangeableDocument.getPropertyValue("dc:description");
		
		return super.getThreadModel();
	}

	@Override
	public boolean isThreadModerated(DocumentModel thread)
			throws ClientException {
		this.moderated = super.isThreadModerated(thread);
		return this.moderated;
	}

	@Override
	public List<String> getModerators() {
		this.selectedModerators = super.getModerators();
		return this.selectedModerators; 			
	}
	
	public String addThread(String viewId) throws ClientException {
		super.addThread();
		return viewId;
	}

	public String updateThread() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();

        currentDocument.setProperty(schema, "moderated", moderated);
        List<String> sM = getSelectedModerators();
        
        if (!moderated) {
        	sM.clear();
        } else {
            // We automatically add administrators (with prefix) as moderators
            if (!sM.contains(NuxeoGroup.PREFIX + SecurityConstants.ADMINISTRATORS)) {
            	sM.add(NuxeoGroup.PREFIX + SecurityConstants.ADMINISTRATORS);
            }
            
            // We can also remove Administrator since his group is added
            if (sM.contains(NuxeoPrincipal.PREFIX + SecurityConstants.ADMINISTRATOR)) {
            	sM.remove(NuxeoPrincipal.PREFIX + SecurityConstants.ADMINISTRATOR);
            }
        }
        setSelectedModerators(sM);
        currentDocument.setProperty(schema, "moderators", this.selectedModerators);
        
        // notifications avant
        Events.instance().raiseEvent(EventNames.BEFORE_DOCUMENT_CHANGED, currentDocument);
        
        // sauvegarde
        currentDocument = documentManager.saveDocument(currentDocument);
        
        // notifications après
        navigationContext.invalidateCurrentDocument();
        facesMessages.add(StatusMessage.Severity.INFO,
                resourcesAccessor.getMessages().get("document_modified"),
                resourcesAccessor.getMessages().get(currentDocument.getType()));
        EventManager.raiseEventsOnDocumentChange(currentDocument);
        return navigationContext.navigateToDocument(currentDocument, "after-edit");
    }
    
	public String updateThread(String viewId) throws ClientException {
		updateThread();
		return viewId;
	}    
    
    @Observer(value = {EventNames.NEW_DOCUMENT_CREATED,
    		EventNames.DOCUMENT_SELECTION_CHANGED}, create = false)
    public void refresh() throws ClientException {
    	super.clean();
    }
	
}
