/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;


/**
 * @author david
 *
 */
public class PreMessageBodyWriter {

    private static final Log log = LogFactory.getLog(PreMessageBodyWriter.class);

    private PreMessageBodyWriter() {
        super();
    }

    public static void prepareResult(Object result) {

        if (result != null) {
            OperationResultType type = OperationResultType.getType(result);

            if (log.isDebugEnabled()) {
                log.debug("Preparing: " + result.toString());
                log.debug("Type: " + type);
            }
            if (type != null)
            {
                switch (type) {
                    case document:
                        DocumentModel doc = (DocumentModel) result;
                        // TODO: to test / versions label
                        refreshDocument(doc);
                        break;
    
                    case documents:
                        DocumentModelList docs = (DocumentModelList) result;
                        for (DocumentModel doc_ : docs) {
                            refreshDocument(doc_);
                        }
                        break;
    
                    default:
                        break;
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(result.toString() + " prepared");
            }

        }

    }

    /**
     * @param doc
     */
    private static void refreshDocument(DocumentModel doc) {
        doc.refresh();
        doc.getLockInfo();
    }

}
