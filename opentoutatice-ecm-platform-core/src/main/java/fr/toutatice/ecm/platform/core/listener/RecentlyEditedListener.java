package fr.toutatice.ecm.platform.core.listener;

import java.security.Principal;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.edition.RecentlyEditedCacheHelper;

/**
 * fills the recentlyModifiedCache when a user edits a document
 *
 * @author dorian
 *
 */
public class RecentlyEditedListener implements EventListener {

    private static final String DOCUMENT_MODIFIED = "documentModified";

    @Override
    public void handleEvent(Event event) throws ClientException {

        EventContext context = event.getContext();

        if (StringUtils.equals(event.getName(), DOCUMENT_MODIFIED) && context instanceof DocumentEventContext) {
            DocumentEventContext docCtx = (DocumentEventContext) context;

            Principal principal = docCtx.getPrincipal();
            if (principal != null) {
                RecentlyEditedCacheHelper.put(docCtx.getSourceDocument(), principal.getName());
            }
        }
    }
}
