/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document.transaction.test;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.automation.document.CreateDocument;


/**
 * @author david
 *
 */
@Operation(id = CreateDocumentInTransaction.ID)
public class CreateDocumentInTransaction {

    public static final String ID = "Transaction.CreateDocument";
    
    private static final Log log = LogFactory.getLog(CreateDocumentInTransaction.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "type")
    protected String type;

    @Param(name = "name", required = false)
    protected String name;

    @Param(name = "properties", required = false)
    protected Properties properties;

    @OperationMethod
    public DocumentModel run(DocumentModel parent) throws Exception {
        DocumentModel result = null;

        log.error("Incoming thread: " + Thread.currentThread());

        Principal principal = this.session.getPrincipal();
        String userName = principal != null ? principal.getName() : StringUtils.EMPTY;

        Map<String, Object> params = new HashMap<>(2);
        params.put("type", this.type);
        params.put("name", this.name);
        params.put("properties", this.properties);

        Callable<Object> conversation = new ConversationThread(this.ctx, CreateDocument.ID, params, userName);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(conversation);

        while (((ConversationThread) conversation).result == null) {
            Thread.sleep(1000);
        }
        // result = (DocumentModel) future.get();

        result = (DocumentModel) ((ConversationThread) conversation).result;
        log.error("Created DOC: " + result.getId());

        // CoreSession coreSession = result.getCoreSession();
        // if (coreSession != null) {
        // DocumentModel checkedParent = coreSession.getDocument(result.getParentRef());
        // if (checkedParent != null) {
        // log.error("SUCCESS!: " + checkedParent.toString());
        // } else {
        // log.error("KO ...: can't acces parent");
        // }
        // } else {
        // log.error("No more session for: " + result.getPathAsString());
        // }

        return result;
    }


}
