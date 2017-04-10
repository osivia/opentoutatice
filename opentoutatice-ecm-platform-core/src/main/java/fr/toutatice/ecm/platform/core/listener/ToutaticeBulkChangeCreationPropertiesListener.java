/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
public class ToutaticeBulkChangeCreationPropertiesListener implements PostCommitFilteringEventListener {

    /** To block this listener. */
    public static final String BLOCK = "block";

    /** Log. */
    private static final Log log = LogFactory.getLog(ToutaticeBulkChangeCreationPropertiesListener.class);

    /** EventService. */
    private static EventService evtService;

    /**
     * Getter for EventService.
     */
    public EventService getEventService() {
        if (evtService == null) {
            evtService = (EventService) Framework.getService(EventService.class);
        }
        return evtService;
    }

    /**
     * Accepts documentCreatedByCopy event.
     */
    @Override
    public boolean acceptEvent(Event event) {
        return DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName());
    }

    /**
     * Recurse change of creator when copied document is a Folder.
     */
    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        // One event: documentCreatedByCopy
        Event event = events.peek();

        // Robustness: DocumentEventContext
        EventContext context = event.getContext();
        if (DocumentEventContext.class.isInstance(context)) {
            DocumentEventContext docCtx = (DocumentEventContext) context;

            if (BooleanUtils.isNotTrue((Boolean) docCtx.getProperty(BLOCK))) {

                // Copied document
                DocumentModel srcDoc = docCtx.getSourceDocument();

                // Only Folders
                if (srcDoc.isFolder() && ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc)) {

                    if (!ToutaticeChangeCreationPropertiesListener.block(docCtx)) {
                        CoreSession session = docCtx.getCoreSession();

                        try {
                            DocumentModelList docs = new DocumentModelListImpl();
                            docs.add(srcDoc);
                            if (session.exists(srcDoc.getRef())) {
                                // Call listeners on createdByCopy
                                fireCreatedByCopy(docCtx, session, event, docs);

                                // Commit
                                session.save();
                            }
                        } catch (ClientException e) {
                            log.error("Unable to get children", e);
                            return;
                        }
                    }
                }
            }
        }

    }

    /**
     * Set user as creator of documents.
     * 
     * @param documentManager
     * @param docs
     * @throws ClientException
     */
    // TODO: test in Publish Spaces, i.e. effect on local proxies
    protected void fireCreatedByCopy(DocumentEventContext docCtx, CoreSession session, Event event, DocumentModelList docs) throws ClientException {
        for (DocumentModel docMod : docs) {

            DocumentEventContext newDocCtx = new DocumentEventContext(session, docCtx.getPrincipal(), docMod);
            Event eventToThrow = new EventImpl(DocumentEventTypes.DOCUMENT_CREATED_BY_COPY, newDocCtx);

            newDocCtx.setProperty(BLOCK, true);

            getEventService().fireEvent(eventToThrow);

            ToutaticeDocumentHelper.saveDocumentSilently(session, docMod, true);

            // Recurse
            if (docMod.isFolder()) {
                DocumentModelList children = session.query(String.format(
                        "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isVersion = 0 AND ecm:currentLifeCycleState != 'deleted' ", docMod.getRef()));
                fireCreatedByCopy(docCtx, session, event, children);
            }
        }
    }


}
