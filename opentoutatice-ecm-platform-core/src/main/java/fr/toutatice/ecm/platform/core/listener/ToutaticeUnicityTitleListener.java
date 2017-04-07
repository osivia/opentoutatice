/**
 * 
 */
package fr.toutatice.ecm.platform.core.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
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
    
    /** Default copied suffix title. */
    public final static String DEFAULT_COPIED_TITLE_SUFFIX = " (copie)";
    /** Default suffix title. */
    public final static String DEFAULT_TITLE_SUFFIX = " (1)";

    /** Title suffix pattern. */
    private static final Pattern TITLE_SUFFIX_PATTERN = Pattern.compile("(.*)\\((copie |copie)?([0-9]+)?\\)(\\.[a-z]+)?$");
    
    /**
     * Checks unicity of document's title
     * if document is in Collaborative Space.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        
        if(event.getContext() instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel document = docCtx.getSourceDocument();
            
            if (ToutaticeDocumentHelper.isInWorkspaceLike(docCtx.getCoreSession(), document)
                    && ToutaticeDocumentEventListenerHelper.isAlterableDocument(document)) {
                // Initial title
                String initialTitle = (String) document.getPropertyValue("dc:title");
                String newTitle = makeUniqueTitle(docCtx, event.getName(), document);

                if (!StringUtils.equals(initialTitle, newTitle)) {
                    document.setPropertyValue("dc:title", newTitle);
                }
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
    protected String makeUniqueTitle(DocumentEventContext docCtx, String eventName, DocumentModel document) {
        CoreSession session = docCtx.getCoreSession();
        String parentUUId = session.getParentDocument(document.getRef()).getId();
        String docUUId = document.getId();
        String title = (String) document.getPropertyValue("dc:title");

        boolean isUniqueTitle = ToutaticeDocumentMetadataHelper.isTileUnique(session, parentUUId, docUUId, title);
        while (!isUniqueTitle) {
            Matcher matcher = TITLE_SUFFIX_PATTERN.matcher(title);

            if (matcher.find()) {
                StringBuffer sb = new StringBuffer();
                String number = matcher.group(3);
                if (number != null) {
                    int num = Integer.valueOf(number).intValue() + 1;
                    number = String.valueOf(num);
                } else {
                    number = " " + String.valueOf(1);
                }
                matcher.appendReplacement(sb, "$1" + "(" + "$2" + number + ")" + "$4");
                title = matcher.appendTail(sb).toString();
            } else {
                
                String fileExtension = StringUtils.substringAfterLast(title, ".");
                if (StringUtils.isNotEmpty(fileExtension)) {
                    fileExtension = ".".concat(fileExtension);
                }
                
                String marker = DEFAULT_TITLE_SUFFIX;
                if (DocumentEventTypes.DOCUMENT_CREATED_BY_COPY.equals(eventName)) {
                    marker = DEFAULT_COPIED_TITLE_SUFFIX;
                } 
                
                title = StringUtils.removeEnd(title, fileExtension).concat(marker)
                        .concat(fileExtension);
            }

            isUniqueTitle = ToutaticeDocumentMetadataHelper.isTileUnique(session, parentUUId, docUUId, title);
        }

        return title;
    }

}
