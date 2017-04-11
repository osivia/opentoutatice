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
public class ToutaticeBulkDocumentCopiedListener implements PostCommitFilteringEventListener {

    /** To block this listener. */
    public static final String BLOCK = "block";

    /** Log. */
    private static final Log log = LogFactory.getLog(ToutaticeBulkDocumentCopiedListener.class);

    /** EventService. */
    private static EventService evtService;

    /**
     * Getter for EventService.
     */
    public static EventService getEventService() {
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

                        DocumentModelList docs = new DocumentModelListImpl();
                        docs.add(srcDoc);
                        if (session.exists(srcDoc.getRef())) {
                            // Call listeners on createdByCopy
                            try {
                                fireCreatedByCopy(docCtx, session, event, docs);
                            } catch (Exception e) {
                                log.error(e);
                            }

                            // Commit
                            session.save();
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
    protected void fireCreatedByCopy(DocumentEventContext docCtx, CoreSession session, Event event, DocumentModelList docs) {
        for (DocumentModel docMod : docs) {

            try {
                throwEvent(docCtx, DocumentEventTypes.DOCUMENT_CREATED_BY_COPY, session, docMod);
            } catch (Exception e) {
                log.error(e);
            }

            // Recurse
            if (docMod.isFolder()) {
                DocumentModelList children = null;
                try {
                    children = session.query(String.format(
                            "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isVersion = 0 AND ecm:currentLifeCycleState <> 'deleted' ",
                            docMod.getRef()));
                } catch (Exception e) {
                    // Possible error on getting children (?)
                    log.error(e);
                }

                if (children != null) {
                    fireCreatedByCopy(docCtx, session, event, children);
                }

            }
        }
    }

    /**
     * 
     * 
     * @param docCtx
     * @param session
     * @param docMod
     */
    public static void throwEvent(DocumentEventContext docCtx, String eventName, CoreSession session, DocumentModel docMod) {
        // Event creation
        DocumentEventContext newDocCtx = new DocumentEventContext(session, docCtx.getPrincipal(), docMod);
        Event eventToThrow = new EventImpl(eventName, newDocCtx);
        // To avoid loop
        newDocCtx.setProperty(BLOCK, true);

        // Fire
        getEventService().fireEvent(eventToThrow);
        // Save document's modification due to inline listeners
        ToutaticeDocumentHelper.saveDocumentSilently(session, docMod, true);
    }


}
