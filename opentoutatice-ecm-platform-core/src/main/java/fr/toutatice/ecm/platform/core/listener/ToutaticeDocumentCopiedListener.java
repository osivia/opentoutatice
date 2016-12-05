/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import static org.nuxeo.ecm.core.schema.FacetNames.SYSTEM_DOCUMENT;

import java.util.Calendar;
import java.util.Date;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.dublincore.NXDublinCore;
import org.nuxeo.ecm.platform.dublincore.listener.DublinCoreListener;
import org.nuxeo.ecm.platform.dublincore.service.DublinCoreStorageService;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
public class ToutaticeDocumentCopiedListener implements EventListener {
    
    /**
     * Changes creator (and creation date) when document is copied.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName()) && event.getContext() instanceof DocumentEventContext) {

            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();

            DublinCoreStorageService service = NXDublinCore.getDublinCoreStorageService();

            Boolean block = (Boolean) docCtx.getProperty(DublinCoreListener.DISABLE_DUBLINCORE_LISTENER);
            if (Boolean.TRUE.equals(block)) {
                return;
            }

            DocumentModel document = docCtx.getSourceDocument();

            if (document.hasFacet(SYSTEM_DOCUMENT)) {
                return;
            }

            Date eventDate = new Date(event.getTime());
            Calendar cEventDate = Calendar.getInstance();
            cEventDate.setTime(eventDate);

            service.setCreationDate(document, cEventDate, event);
            service.setModificationDate(document, cEventDate, event);
            service.addContributor(document, event);
            // We have to set creator cause addContributor doesn't do it if 
            // this field is not empty
            document = setCreator(docCtx, document);
            
            ToutaticeDocumentHelper.saveDocumentSilently(docCtx.getCoreSession(), document, true);
        }
    }

    /**
     * Sets creator.
     * 
     * @param docCtx
     * @param document
     * @return document updated
     */
    protected DocumentModel setCreator(DocumentEventContext docCtx, DocumentModel document){
        document.setProperty("dublincore", "creator", docCtx.getPrincipal().getName());
        return document;
    }

}
