/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

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
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;


/**
 * @author david
 *
 */
public class ToutaticeBulkChangeCreationPropertiesListener implements PostCommitFilteringEventListener {

    /** Log. */
    private static final Log log = LogFactory.getLog(ToutaticeBulkChangeCreationPropertiesListener.class);

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

            // Copied document
            DocumentModel srcDoc = docCtx.getSourceDocument();

            if (!ToutaticeChangeCreationPropertiesListener.block(docCtx)) {
                // Only Folders
                if (srcDoc.isFolder()) {
                    CoreSession session = docCtx.getCoreSession();

                    // // Initiator of copy will be set as creator
                    try {
                        DocumentModelList docs = new DocumentModelListImpl();
                        docs.add(srcDoc);
                        if (session.exists(srcDoc.getRef())) {
                            changeCreationProperties(docCtx, session, event, docs);
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

    /**
     * Set user as creator of documents.
     * 
     * @param documentManager
     * @param docs
     * @throws ClientException
     */
    // TODO: test in Publish Spaces, i.e. effect on local proxies
    protected void changeCreationProperties(DocumentEventContext docCtx, CoreSession documentManager, Event event, DocumentModelList docs)
            throws ClientException {
        for (DocumentModel docMod : docs) {
            ToutaticeChangeCreationPropertiesListener.changeCreationProperties(event, docCtx, docMod);
            if (docMod.isFolder()) {
                DocumentModelList children = documentManager.query(String.format(
                        "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isVersion = 0 AND ecm:currentLifeCycleState != 'deleted' ", docMod.getRef()));
                changeCreationProperties(docCtx, documentManager, event, children);
            }
        }
    }


}
