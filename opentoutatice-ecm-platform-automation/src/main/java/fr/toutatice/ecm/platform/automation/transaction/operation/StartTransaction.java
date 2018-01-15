/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversationManager;


/**
 * @author david
 */
@Operation(id = StartTransaction.ID)
public class StartTransaction {

    public static final String ID = "Repository.StartTransaction";

    private static final Log log = LogFactory.getLog(StartTransaction.class);

    @Context
    protected CoreSession session;

    @OperationMethod
    public Blob run() throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Starting transaction ");
        }

        String txId = TransactionalConversationManager.getInstance().start(this.session.getPrincipal(), this.session.getRepositoryName());

        if (log.isDebugEnabled()) {
            log.debug("Transaction " + txId + " started");
        }

        return new StringBlob(txId);
    }

}
