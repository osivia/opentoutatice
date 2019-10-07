/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.operation;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversationManager;

/**
 * @author david
 */
@Operation(id = CommitOrRollbackTransaction.ID)
public class CommitOrRollbackTransaction {

    public static final String ID = "Repository.CommitOrRollbackTransaction";
    
    @OperationMethod
    public Blob run() throws Exception {
        return null;
    }
    

}
