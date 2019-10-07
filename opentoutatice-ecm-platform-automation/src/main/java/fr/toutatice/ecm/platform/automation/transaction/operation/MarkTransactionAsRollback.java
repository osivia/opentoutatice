package fr.toutatice.ecm.platform.automation.transaction.operation;

import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;

/**
 * @author david
 */
@Operation(id = MarkTransactionAsRollback.ID)
public class MarkTransactionAsRollback {

    public static final String ID = "Repository.MarkTransactionAsRollback";
    
    @OperationMethod
    public Blob run() throws Exception {
        return null;
    }

}
