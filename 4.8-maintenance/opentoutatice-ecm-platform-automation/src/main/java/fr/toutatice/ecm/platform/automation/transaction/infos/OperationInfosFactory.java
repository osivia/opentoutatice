/**
 * 
 */
package fr.toutatice.ecm.platform.automation.transaction.infos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author david
 *
 */
public class OperationInfosFactory {

    private Map<String, OperationInfos> opInfosByTx;

    private static OperationInfosFactory instance;

    public OperationInfosFactory() {
        this.opInfosByTx = new ConcurrentHashMap<>();
    }

    private static OperationInfosFactory get() {
        if (instance == null) {
            instance = new OperationInfosFactory();
        }
        return instance;
    }


    public static OperationInfos buildOperationInfos(String txId, String opId) {
        OperationInfos opInfos = new OperationInfos();
        opInfos.setOpId(opId);

        get().addOperationInfos(txId, opInfos);

        return opInfos;
    }

    private void addOperationInfos(String txId, OperationInfos opInfos) {
        get().opInfosByTx.put(txId, opInfos);
    }

    public static OperationInfos getOperationInfos(String txId) {
        return get().opInfosByTx.get(txId);
    }

}
