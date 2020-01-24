package fr.toutatice.ecm.platform.automation.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This thread will autocommit pending transactions
 */
public class TransactionsSupervisor  implements Callable<Object> {
    
    private static Map<String,Long> currentConversations = new ConcurrentHashMap<String,Long>();
    
    TransactionalConversationManager txManager;
    
    private static long MAX_ELAPSED_TIME= 60000L;

    
    private static final Log log = LogFactory.getLog(TransactionsSupervisor.class);

    
    public TransactionsSupervisor(TransactionalConversationManager txManager) {
        super();
        this.txManager = txManager;
    }

    public static void addConversation( String txId)  {
        currentConversations.put(txId, System.currentTimeMillis());
    }
    
    public static void removeConversation( String txId)  {
        currentConversations.remove(txId);
    }
       
    

    @Override
    public Object call() throws Exception {
        while(true) {
            
            Thread.sleep(1000L);
            
            long current = System.currentTimeMillis();
            
            // Creation temporary map to allow concurrent updates
            Map<String, Long> tempConversations = new HashMap<String, Long>();
            for (Entry<String, Long> entry : currentConversations.entrySet())   {
                tempConversations.put(entry.getKey(), entry.getValue());
            }
            
            // Iterate to find pending transactions
            for (Entry<String, Long> entry : tempConversations.entrySet())   {
                
                long elapsedTime = current - entry.getValue().longValue();

                if( elapsedTime  > MAX_ELAPSED_TIME)  {
                    log.warn("rollback pending transaction " + tempConversations.size());
                    txManager.forceRollback(entry.getKey());
                 }
            }
        }
    }

}
