/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document.transaction.test;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;

import fr.toutatice.ecm.platform.automation.transaction.TransactionalConversationManager;

/**
 * @author david
 */
@Operation(id = ExceptionTest.ID)
public class ExceptionTest {

    public static final String ID = "Transaction.GenerateExceptionTest";
    
    @OperationMethod
    public Blob run() throws Exception {
        throw new Exception ("test exception");
    }
    

}
