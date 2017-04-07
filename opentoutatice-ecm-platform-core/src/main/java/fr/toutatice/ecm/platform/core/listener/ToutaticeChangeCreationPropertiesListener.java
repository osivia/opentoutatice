/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import static org.nuxeo.ecm.core.schema.FacetNames.SYSTEM_DOCUMENT;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.BooleanUtils;
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
public class ToutaticeChangeCreationPropertiesListener implements EventListener {
    
    /** DublinCore service. */
    private static DublinCoreStorageService dcService;

    /**
     * Getter for DublinCore service.
     */
    public static DublinCoreStorageService getDublinCoreStorageService() {
        if (dcService == null) {
            dcService = NXDublinCore.getDublinCoreStorageService();
        }
        return dcService;
    }

    /**
     * Changes creator (and creation date) when document is copied.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(event.getName()) && event.getContext() instanceof DocumentEventContext) {

            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();

            if (!block(docCtx)) {
                DocumentModel srcDoc = docCtx.getSourceDocument();
                srcDoc = changeCreationProperties(event, docCtx, srcDoc);

                ToutaticeDocumentHelper.saveDocumentSilently(docCtx.getCoreSession(), srcDoc, true);
            }
        }
    }

    /**
     * Changes DublinCore properties.
     * 
     * @param event
     * @param docCtx
     * @param service
     * @param srcDoc
     * @return updated document
     */
    public static DocumentModel changeCreationProperties(Event event, DocumentEventContext docCtx, DocumentModel srcDoc) {
        Date eventDate = new Date(event.getTime());
        Calendar cEventDate = Calendar.getInstance();
        cEventDate.setTime(eventDate);

        DublinCoreStorageService service = getDublinCoreStorageService();

        service.setCreationDate(srcDoc, cEventDate, event);
        service.setModificationDate(srcDoc, cEventDate, event);
        service.addContributor(srcDoc, event);
        // We have to set creator cause addContributor doesn't do it if
        // this field is not empty
        return setCreator(docCtx, srcDoc);
    }

    /**
     * Blocks modification if necessary.
     * 
     * @param docCtx
     * @return block
     */
    public static boolean block(DocumentEventContext docCtx) {
        // DublinCoreListener status
        Boolean disableDC = (Boolean) docCtx.getProperty(DublinCoreListener.DISABLE_DUBLINCORE_LISTENER);
        return BooleanUtils.isTrue(disableDC) || docCtx.getSourceDocument().hasFacet(SYSTEM_DOCUMENT);
    }

    /**
     * Sets creator.
     * 
     * @param docCtx
     * @param document
     * @return document updated
     */
    public static DocumentModel setCreator(DocumentEventContext docCtx, DocumentModel document) {
        document.setProperty("dublincore", "creator", docCtx.getPrincipal().getName());
        return document;
    }

}
