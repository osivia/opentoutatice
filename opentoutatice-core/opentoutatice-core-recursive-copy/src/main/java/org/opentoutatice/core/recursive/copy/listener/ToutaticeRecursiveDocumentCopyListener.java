/**
 * 
 */
package org.opentoutatice.core.recursive.copy.listener;

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
import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;


/**
 * Recurse copy is done at low level (raws of BDD) in Nuxeo and, at high level (CoreSession), 
 * when the copied document is folderish, the 'documentCreatedByCopy' event is only fired for this folder
 * and not its possible descendants.
 * This listener has been created to avoid to implement two listeners when a hierarchy of documents is copied
 * (one inline for the root and one asynchronous postcommit for its descendants) 
 * firing a 'documentCreatedByCopy' event for each descendants of hierarchy.
 * 
 * @author david
 *
 */
public class ToutaticeRecursiveDocumentCopyListener implements PostCommitFilteringEventListener {

    /** To block this listener. */
    public static final String BLOCK_SELF_CALL = "block_self_call";

    /** Log. */
    private static final Log log = LogFactory.getLog(ToutaticeRecursiveDocumentCopyListener.class);

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
     * Fire a 'documentCreatedByCopy' event on descendants of copied root document. 
     */
    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        // One event: documentCreatedByCopy
        Event event = events.peek();

        // Robustness: DocumentEventContext
        EventContext context = event.getContext();
        if (DocumentEventContext.class.isInstance(context)) {
            DocumentEventContext docCtx = (DocumentEventContext) context;

            if (BooleanUtils.isNotTrue((Boolean) docCtx.getProperty(BLOCK_SELF_CALL))) {

                // Copied document and its session
                DocumentModel srcDoc = docCtx.getSourceDocument();
                CoreSession session = docCtx.getCoreSession();
                
                // Asynchronous listener so check existence
				if (session.exists(srcDoc.getRef())) {
					// Only Folders
					if (srcDoc.isFolder() && ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc)) {
						// Copied root document is yet treated by inline listener
						// so skip it and treat its first children
						DocumentModelList firstChildren = getChildren(session, srcDoc);
						if (firstChildren != null) {
							DocumentModelList docs = new DocumentModelListImpl();
							docs.addAll(firstChildren);
							// Call (inline) listeners on createdByCopy
							try {
								if (log.isDebugEnabled()) {
									log.debug(String.format("About to fire 'createdByCopy' event for [%s] children",
											srcDoc.getPathAsString()));
								}

								fireCreatedByCopy(docCtx, session, event, docs);
							} catch (Exception e) {
								// Do not block global tree copy
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
     * Fire a 'documentCreatedByCopy' event recursively.
     * 
     * @param docCtx
     * @param session
     * @param event
     * @param docs
     */
    // TODO: test in Publish Spaces, i.e. effect on local proxies
    protected void fireCreatedByCopy(DocumentEventContext docCtx, CoreSession session, Event event, DocumentModelList docs) {
    	
        for (DocumentModel docMod : docs) {

            try {
                throwEvent(docCtx, DocumentEventTypes.DOCUMENT_CREATED_BY_COPY, session, docMod);
            } catch (Exception e) {
            	// Do not block global tree copy
                log.error(e);
            }

            // Recursive
            if (docMod.isFolder()) {
                DocumentModelList children = getChildren(session, docMod);
                if (children != null) {
                    fireCreatedByCopy(docCtx, session, event, children);
                }

            }
        }
    }

	private DocumentModelList getChildren(CoreSession session, DocumentModel docMod) {
		return session.query(String.format(
		            "SELECT * FROM Document WHERE ecm:parentId = '%s' AND ecm:isVersion = 0 AND ecm:currentLifeCycleState <> 'deleted' ",
		            docMod.getRef()));
	}

    /**
     * 
     * 
     * @param docCtx
     * @param session
     * @param docMod
     */
    public static void throwEvent(DocumentEventContext docCtx, String eventName, CoreSession session, DocumentModel docMod) {
    	if(log.isDebugEnabled()) {
    		log.debug(String.format("Firing 'createdByCopy' event for [%s]", docMod.getPathAsString()));
    	}
    	
        // Event creation
        DocumentEventContext newDocCtx = new DocumentEventContext(session, docCtx.getPrincipal(), docMod);
        Event eventToThrow = new EventImpl(eventName, newDocCtx);
        // Block async call to itself (avoid loop)
        newDocCtx.setProperty(BLOCK_SELF_CALL, true);

        // Fire
        getEventService().fireEvent(eventToThrow);
        // Save document's modification due to inline listeners
        ToutaticeDocumentHelper.saveDocumentSilently(session, docMod, true);
    }


}
