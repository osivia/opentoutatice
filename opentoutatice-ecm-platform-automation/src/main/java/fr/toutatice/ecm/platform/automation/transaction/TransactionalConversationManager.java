/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.transaction.SystemException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.local.ClientLoginModule;
import org.nuxeo.ecm.core.api.local.LoginStack.Entry;

import fr.toutatice.ecm.platform.automation.transaction.operation.CommitOrRollbackTransaction;
import fr.toutatice.ecm.platform.automation.transaction.operation.MarkTransactionAsRollback;


/**
 * @author david
 *
 */
public class TransactionalConversationManager {
    
    private static final Log log = LogFactory.getLog(TransactionalConversationManager.class);
    
    private static final String TXC_ID_PREFIX = "TXC-";
    
    private static TransactionalConversationManager instance;

    private TransactionalConversationPool txPool;
    
    private ExecutorService supervisorExecutor;
    
    private long txCounter = 0;

    private TransactionalConversationManager() {
        super();
        this.txPool = new TransactionalConversationPool();
        
        this.supervisorExecutor = Executors.newSingleThreadExecutor();
        supervisorExecutor.submit(new TransactionsSupervisor(this));
    }

    public static synchronized TransactionalConversationManager getInstance() {
        if (instance == null) {
            instance = new TransactionalConversationManager();
        }
        return instance;
    }
    
  
    /**
     * Start transaction
     * @param principal
     * @param repositoryName
     * @return
     * @throws SystemException
     */
    public String start(Principal principal, String repositoryName) throws SystemException {
        String txcId = null;
        
        //TransactionalConversation must be executed in the same thread
        //To do this, we use Executors.newSingleThreadExecutor() and keep it in the TransactionalConversation to use the same thread to call TransactionalConversation.call
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Entry loginStack = ClientLoginModule.getThreadLocalLogin().peek();
        
        
        TransactionalConversation txConv = new TransactionalConversation(principal, repositoryName, executor, loginStack);

        
        Future<Object> future = txConv.getExecutor().submit(txConv);
        try {
            future.get();
            txcId = getTxcId(this.txCounter);
            this.txCounter++;
            txConv.setTxcId(txcId);
            this.txPool.put(txcId, txConv);
             
            TransactionsSupervisor.addConversation(txcId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new SystemException();
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new SystemException();
        }

        return txcId;
    }

    /**
     * Operation call in the transaction
     * @param txId
     * @param ctx
     * @param opId
     * @param params
     * @return
     * @throws InterruptedException
     */
    public Object notify(String txId, Object ctx, Object opId, Object params) throws InterruptedException {
        TransactionalConversation txConv = getTxConv(txId);
        if (txConv != null) {
            txConv.setOperationContext((OperationContext) ctx);
            txConv.setParams((Map<String, Object>) params);
            txConv.setOperationId((String) opId);

            Future<Object> future = txConv.getExecutor().submit(txConv);
            try {
                Object resultat = future.get();
                if (StringUtils.equals(CommitOrRollbackTransaction.ID, (String) opId)) 
                {
                    stopThread(txId);
                }
                return resultat;
            } catch (ExecutionException e) {
                log.warn("Thread return exception :"+ e.getMessage()+ ". Force rollback");
                // Avoid loop
                if( !(StringUtils.equals(CommitOrRollbackTransaction.ID, (String) opId)) &&   !(StringUtils.equals(MarkTransactionAsRollback.ID, (String) opId)))
                    forceRollback(txId);
            }
            
        } else {
            log.error("No transaction found with this transaction id:"+txId);
        }
        return null;
    }

    public TransactionalConversation get(String txId) {
        return this.txPool.get(txId);
    }

    
    
    /**
     * Auto commit the transaction by notifying the thread
     *
     * @param txId the tx id
     */
    protected void forceRollback(String txId) {
        try {
            notify(txId, null, MarkTransactionAsRollback.ID, null);
            notify(txId, null, CommitOrRollbackTransaction.ID, null);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }
    
      
    
    /**
     * Auto commit the transaction by notifying the thread
     *
     * @param txId the tx id
     */
    protected void autoCommit(String txId) {

        try {
            notify(txId, null, CommitOrRollbackTransaction.ID, null);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

    }

    
    private void stopThread(String txId) {
        
        TransactionalConversation txConv = getTxConv(txId);
        if( txConv != null) {
            txConv.getExecutor().shutdown();
            txPool.remove(txId);
            TransactionsSupervisor.removeConversation( txId);
        }

    }

    /**
     * @param txId
     * @return
     */
    private TransactionalConversation getTxConv(String txId) {
        return this.txPool.get(txId);
    }

    public String getTxcId(long txc) {
        return TXC_ID_PREFIX + String.valueOf(txc);
    }
    
}
