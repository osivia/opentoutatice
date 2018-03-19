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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.usermanager.UserManager;

import fr.toutatice.ecm.platform.automation.FetchPublicationInfos;


/**
 * @author david
 *
 */
@Operation(id = "FetchPubInfosInTransaction")
public class FetchPubInfosInTransaction {

    private static final Log log = LogFactory.getLog(FetchPubInfosInTransaction.class);
    
    @Context
    protected OperationContext ctx;

    /**
     * Session.
     */
    @Context
    protected CoreSession coreSession;

    /**
     * Service gérant les types.
     */
    @Context
    protected TypeManager typeService;

    /**
     * Service gérant les utilisateurs.
     */
    @Context
    protected UserManager userManager;

    /**
     * Identifiant ("path" ou uuid) du document en entrée.
     */
    @Param(name = "path", required = false)
    protected DocumentModel document;

    @Param(name = "webid", required = false)
    protected String webid;
    
    @OperationMethod
    public Blob run() throws Exception {

        log.error("Incoming thread: " + Thread.currentThread());

        Principal principal = this.coreSession.getPrincipal();
        String userName = principal != null ? principal.getName() : StringUtils.EMPTY;

        Map<String, Object> params = new HashMap<>(2);
        params.put("path", this.document);
        params.put("webid", this.webid);

        Callable<Object> conversation = new ConversationThread(this.ctx, FetchPublicationInfos.ID, params, userName);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(conversation);

        while (((ConversationThread) conversation).result == null) {
            Thread.sleep(1000);
        }

        return (Blob) ((ConversationThread) conversation).result;

    }
}
