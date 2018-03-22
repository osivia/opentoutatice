/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.operation;
import org.nuxeo.ecm.automation.core.annotations.Operation;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversationManager;

/**
 * @author david
 */
@Operation(id = CommitOrRollbackTransaction.ID)
public class CommitOrRollbackTransaction {

    public static final String ID = "Repository.CommitOrRollbackTransaction";

}
