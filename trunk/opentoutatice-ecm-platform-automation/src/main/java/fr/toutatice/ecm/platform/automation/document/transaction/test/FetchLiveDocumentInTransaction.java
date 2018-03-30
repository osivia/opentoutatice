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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

import fr.toutatice.ecm.platform.automation.FetchLiveDocument;


/**
 * @author david
 *
 */
@Operation(id = "FetchLiveDocumentInTransaction")
public class FetchLiveDocumentInTransaction {

    private static final Log log = LogFactory.getLog(FetchLiveDocumentInTransaction.class);

    private static final String AND = "AND";

    private static final String OR = "OR";

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession session;

    @Param(name = "value", required = true)
    protected DocumentModel value;

    @Param(name = "permission", required = false, values = {SecurityConstants.READ, SecurityConstants.READ_WRITE, SecurityConstants.WRITE})
    protected String permission = SecurityConstants.WRITE;

    @Param(name = "operation", required = false, values = {OR, AND})
    protected String operation = OR;

    @OperationMethod
    public Object run() throws Exception {

        log.error("Incoming thread: " + Thread.currentThread());

        Principal principal = this.session.getPrincipal();
        String userName = principal != null ? principal.getName() : StringUtils.EMPTY;

        Map<String, Object> params = new HashMap<>(2);
        params.put("value", this.value);

        Callable<Object> conversation = new ConversationThread(this.ctx, FetchLiveDocument.ID, params, userName);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(conversation);

        while (((ConversationThread) conversation).result == null) {
            Thread.sleep(1000);
        }

        return (DocumentModel) ((ConversationThread) conversation).result;
    }

}
