/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentMetadataHelper;


/**
 * @author david
 *
 */
public class ToutaticeUnicityTitleListener implements EventListener {

    /** Copied title suffix pattern. */
    private static final Pattern TITLE_SUFFIX_PATTERN = Pattern.compile("(.*)\\(([0-9]+)+\\)$");

    /**
     * Checks unicity of document's title
     * if document is in Collaborative Space.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        
        if(event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel document = docCtx.getSourceDocument();
            
            if(ToutaticeDocumentHelper.isInWorkSpace(docCtx.getCoreSession(), document)){
                document = checksUnicityTitle(docCtx, document);
                ToutaticeDocumentHelper.saveDocumentSilently(docCtx.getCoreSession(), document, true);
            }
        }

    }
    
    /**
     * Checks unicity document's title.
     * 
     * @param docCtx
     * @param document
     * @return true if title is unique
     */
    protected DocumentModel checksUnicityTitle(DocumentEventContext docCtx, DocumentModel document) {
        CoreSession session = docCtx.getCoreSession();
        String parentUUId = session.getParentDocument(document.getRef()).getId();
        String title = (String) document.getPropertyValue("dc:title");

        boolean isUniqueTitle = ToutaticeDocumentMetadataHelper.isTileUnique(session, parentUUId, null, title);
        while (!isUniqueTitle) {
            Matcher matcher = TITLE_SUFFIX_PATTERN.matcher(title);

            if (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                int number = Integer.valueOf(matcher.group(2));
                matcher.appendReplacement(sb, "$1" + "(" + String.valueOf(number + 1) + ")");
                title = matcher.appendTail(sb).toString();
            } else {
                title = title.concat(" (" + String.valueOf(1) + ")");
            }

            isUniqueTitle = ToutaticeDocumentMetadataHelper.isTileUnique(session, parentUUId, null, title);
        }
        document.setPropertyValue("dc:title", title);

        return document;
    }

}
