/**
 * 
 */
package fr.toutatice.ecm.platform.automation.blob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


/**
 * @author david
 *
 */
public class BlobHelper {

    /** Default document's schema to set blobs. */
    private static final String DEFAULT_SCHEMA = "files:files";

    /**
     * Utility class
     */
    private BlobHelper() {
        super();
    }

    /**
     * Sets blobs in files:files schema of document without saving document.
     * 
     * @param doc
     * @param blobs
     * @return
     */
    public static DocumentModel setBlobs(DocumentModel doc, BlobList blobs) {
        return setBlobs(null, doc, blobs, DEFAULT_SCHEMA, false);
    }

    /**
     * Sets blobs in files:files schema of document, saving or not document.
     * 
     * @param session
     * @param doc
     * @param blobs
     * @param save
     * @return blobs
     */
    public static DocumentModel setBlobs(CoreSession session, DocumentModel doc, BlobList blobs, boolean save) {
        return setBlobs(session, doc, blobs, DEFAULT_SCHEMA, save);
    }

    /**
     * @param blobs
     * @return
     */
    public static DocumentModel setBlobs(CoreSession session, DocumentModel doc, BlobList blobs, String xpath, boolean save) {
        if (CollectionUtils.isNotEmpty(blobs)) {

            List<Map<String, Serializable>> existingBlobs = (List<Map<String, Serializable>>) doc.getPropertyValue(xpath);
            if (existingBlobs == null) {
                existingBlobs = new ArrayList<>();
            }

            for (Blob blob : blobs) {
                Map<String, Serializable> blobProp = DocumentHelper.createBlobHolderMap(blob);
                existingBlobs.add(blobProp);
            }

            doc.setPropertyValue(xpath, (Serializable) existingBlobs);

            if (save) {
                session.saveDocument(doc);
            }

        }

        return doc;
    }

}
