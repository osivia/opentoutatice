/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author Loïc Billon
 * When a folderish document is created, set it show in menu
 *
 */
public class FolderishDocumentCreatedListener implements EventListener {
    
    /** Logger. */
    private static final Log log = LogFactory.getLog(FolderishDocumentCreatedListener.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleEvent(Event event) throws ClientException {
		
		EventContext context = event.getContext();
		
		if(context instanceof DocumentEventContext) {
			
			DocumentEventContext docCtx = (DocumentEventContext) context;
			DocumentModel sourceDocument = docCtx.getSourceDocument();
			if(sourceDocument.isFolder()) {
			    try {
			        sourceDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_SIM, Boolean.TRUE);
			    } catch (PropertyNotFoundException pnf) {
			        if(log.isDebugEnabled()){
			            log.debug("Document ".concat(sourceDocument.getType()).concat(" doesn't have toutatice schema."));
			        }
			    }
			}
			
		}
		
		
	}

}
