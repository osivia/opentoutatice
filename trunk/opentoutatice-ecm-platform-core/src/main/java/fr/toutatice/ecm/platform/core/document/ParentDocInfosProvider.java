/**
 * 
 */
package fr.toutatice.ecm.platform.core.document;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;


/**
 * @author david
 *
 */
public class ParentDocInfosProvider implements DocumentInformationsProvider {

    /** Logger. */
    private static final Log log = LogFactory.getLog(ParentDocInfosProvider.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession session, DocumentModel currentDocument) throws ClientException {
        // Infos
        Map<String, Object> infos = new HashMap<String, Object>(1);

        // Only for Remote proxy
        if (currentDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)) {
            // Parent webId
            GetUnrestrictedParentWebId runner = new GetUnrestrictedParentWebId(session, currentDocument);
            runner.runUnrestricted();

            infos.put("parentWebId", runner.get());
        }

        return infos;
    }

    /**
     * Gets parent's webId in unretricted way.
     */
    private class GetUnrestrictedParentWebId extends UnrestrictedSessionRunner {

        // Document
        private DocumentModel document;
        // Parent
        private String webId;

        protected GetUnrestrictedParentWebId(CoreSession session, DocumentModel document) {
            super(session);
            this.document = document;
        }

        @Override
        public void run() throws ClientException {
            DocumentModel parent = this.session.getParentDocument(this.document.getRef());
            if (parent != null && parent.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
                try {
                    this.webId = (String) parent.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
                } catch (PropertyNotFoundException pnfe) {
                    // Just log
                    log.error("Document " + parent.getPathAsString() + " has no webId");
                }
            }
        }

        // Getter for parent's webId
        public String get() {
            return this.webId;
        }

    }

}
