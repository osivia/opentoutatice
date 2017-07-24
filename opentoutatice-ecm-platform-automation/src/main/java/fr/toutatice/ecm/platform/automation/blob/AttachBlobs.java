package fr.toutatice.ecm.platform.automation.blob;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * 
 * @author david
 *
 */
@Operation(id = AttachBlobs.ID, category = Constants.CAT_BLOB, label = "Attach Files")
public class AttachBlobs {

    public static final String ID = "Blob.AttachList";

    @Context
    protected CoreSession session;

    @Param(name = "xpath", required = false)
    protected String xpath = "files:files";

    @Param(name = "document")
    protected DocumentModel doc;

    @Param(name = "save", required = false, values = "true")
    protected boolean save = true;

    @OperationMethod
    public DocumentModel run(BlobList blobs) throws Exception {
        return setBlobs(blobs);
    }

    /**
     * @param blobs
     * @return
     */
    public DocumentModel setBlobs(BlobList blobs) {
        return BlobHelper.setBlobs(this.session, this.doc, blobs, this.xpath, this.save);
    }

}
