/**
 * 
 */
package org.opentoutatice.core.io.download;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.download.DownloadServiceImpl;


/**
 * To enable possibility to resolve docment's blob on document creation.
 * 
 * @author david
 */
public class OttcDownloadServiceImpl extends DownloadServiceImpl {

    public static final String CREATING_DOC_INDICATOR = "creatingDoc";

    @Override
    protected DocumentModel getDownloadDocument(String repository, String docId) {
        DocumentModel doc = null;

        if (StringUtils.contains(docId, CREATING_DOC_INDICATOR)) {
            // Get document in specific cache
            doc = TransientDocumentCache.getChangeablDocument(docId);
        } else {
            doc = super.getDownloadDocument(repository, docId);
        }

        return doc;
    }

}
