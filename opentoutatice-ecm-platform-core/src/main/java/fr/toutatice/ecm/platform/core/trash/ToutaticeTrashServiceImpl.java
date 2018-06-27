package fr.toutatice.ecm.platform.core.trash;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.trash.TrashServiceImpl;
import org.nuxeo.runtime.api.Framework;

/**
 * @author Loïc Billon
 */
public class ToutaticeTrashServiceImpl extends TrashServiceImpl {

	public final static String EVENT_NAME_DOC_TRASHED = "ttcDocumentTrashed";
	
	@Override
	protected void trashDocument(CoreSession session, DocumentModel doc) throws ClientException {
		
		super.trashDocument(session, doc);
		
		EventService service = Framework.getService(EventService.class);
		service.fireEvent(EVENT_NAME_DOC_TRASHED, new DocumentEventContext(session, session.getPrincipal(), doc));
		
	}
}
