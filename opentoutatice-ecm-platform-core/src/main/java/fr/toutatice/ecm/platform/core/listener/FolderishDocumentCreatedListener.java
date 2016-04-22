/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * @author Loïc Billon
 * When a folderish document is created, set it show in menu
 *
 */
public class FolderishDocumentCreatedListener implements EventListener{

	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.core.event.EventListener#handleEvent(org.nuxeo.ecm.core.event.Event)
	 */
	@Override
	public void handleEvent(Event event) throws ClientException {
		
		EventContext context = event.getContext();
		
		if(context instanceof DocumentEventContext) {
			
			DocumentEventContext docCtx = (DocumentEventContext) context;
			DocumentModel sourceDocument = docCtx.getSourceDocument();
			if(sourceDocument.hasFacet("Folderish")) {
				sourceDocument.setPropertyValue("ttc:showInMenu", "true");
			}
			
		}
		
		
	}

}
