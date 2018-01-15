/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document.transaction.test;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.jtajca.NuxeoContainer;
import org.nuxeo.runtime.transaction.TransactionHelper;


/**
 * @author david
 *
 */
public class ConversationThread implements Callable<Object> {

    private static final Log log = LogFactory.getLog(ConversationThread.class);

    private Transaction tx;
    private CoreSession session;

    private TransactionManager tm = NuxeoContainer.getTransactionManager();
    private AutomationService srv = Framework.getService(AutomationService.class);

    private String userName;

    private String opId;
    private OperationContext callingCtx;
    private Map<String, Object> params;

    public Object result;

    public ConversationThread(OperationContext callingCtx, String opId, Map<String, Object> parameters, String userName) {
        super();
        this.userName = userName;

        this.opId = opId;
        this.callingCtx = callingCtx;
        this.params = parameters;
    }

    @Override
    public Object call() throws Exception {

        log.error("New thread: " + Thread.currentThread());

        // boolean commit = true;
        try {
            // Start new transaction
            // if (this.tx == null) {
            // this.tm.begin();
            TransactionHelper.startTransaction();
            // }

            // start new session
            if (this.session == null) {
                this.session = CoreInstance.openCoreSession(null, this.userName);
            }

            // Create
            OperationContext ctx = new OperationContext(this.session);
            ctx.setInput(this.callingCtx.getInput());
            ctx.setCommit(false);

            result = this.srv.run(ctx, this.opId, this.params);

            if (this.result instanceof DocumentModel) {
                ((DocumentModel) result).isCheckedOut();
                ((DocumentModel) result).getLockInfo();

                for (String schema : ((DocumentModel) result).getSchemas()) {
                    ((DocumentModel) result).getPart(schema);
                }
            }

            if (this.result instanceof DocumentModelList) {
                for (DocumentModel doc : (DocumentModelList) this.result) {
                    doc.isCheckedOut();
                    doc.getLockInfo();
                }
            }

            Thread.sleep(100000);

            // "Commit"
            this.session.save();

        } catch (Exception e) {
            // this.tm.rollback();
            // commit = false;

            TransactionHelper.setTransactionRollbackOnly();

            log.error(e);
        } finally {
            if (this.session != null) {
                this.session.close();
            }

            // if (this.tx != null && commit) {
            // this.tm.commit();
            // }

            TransactionHelper.commitOrRollbackTransaction();
        }

        return result;

    }

}
