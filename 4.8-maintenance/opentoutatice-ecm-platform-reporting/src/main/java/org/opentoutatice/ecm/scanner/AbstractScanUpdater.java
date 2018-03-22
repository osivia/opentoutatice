/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestMode;
import org.opentoutatice.ecm.reporting.test.mode.ErrorTestModeException;


/**
 * @author david
 *
 */
public abstract class AbstractScanUpdater {

    /** Logger. */
    private static final Log log = LogFactory.getLog(AbstractScanUpdater.class);

    /** Updater parameters. */
    private Map<String, Serializable> cfgParams = new HashMap<>(0);

    /**
     * Default constructor.
     */
    public AbstractScanUpdater() {
        super();
    }


    /**
     * @param cfgParams the cfgParams to set
     */
    public void setCfgParams(Map<String, Serializable> cfgParams) {
        this.cfgParams = cfgParams;
    }

    /**
     * Gets parameters adapted from configuration.
     * 
     * @return
     * @throws Exception
     */
    public Map<String, Serializable> getParams() throws Exception {
        return this.cfgParams;
    }

    /**
     * Adapts directive results to other object if necessary.
     * 
     * @param scannedObject
     * @return Object
     * @throws Exception
     */
    public abstract Object toModel(Object scannedObject) throws Exception;

    /**
     * Filters object in transaction.
     * 
     * @param index
     * @param scannedObject
     * @return boolean
     * @throws Exception
     */
    public boolean acceptInNewTx(int index, Object scannedObject) throws Exception {
        // Result
        boolean accepted = false;

        // For error mode test
        boolean acceptedInErrTest = true;

        // We are yet in one transaction, cf EventJob#execute
        Transaction originateTx = TransactionHelper.requireNewTransaction();
        try {
            accepted = accept(index, scannedObject);

            // Error test mode
            if (accepted && ErrorTestMode.generateError(1)) {
                throw new ErrorTestModeException("Error on ScanUpdater#accept");
            }

            return accepted;
        } catch (Exception e) {
            TransactionHelper.setTransactionRollbackOnly();

            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                acceptedInErrTest = false;
            }

            throw e;
        } finally {
            if (ErrorTestMode.isActivated() && accepted && log.isInfoEnabled()) {
                if (acceptedInErrTest) {
                    log.info("Accepted");
                } else {
                    log.info("NOT accepted");
                }
            }

            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.resumeTransaction(originateTx);
        }
    }

    /**
     * Filters object.
     * 
     * @param index
     * @param scannedObject
     * @return boolean
     * @throws Exception
     */
    public abstract boolean accept(int index, Object scannedObject) throws Exception;

    /**
     * Initialize scanned object in transaction if necessary.
     * 
     * @param index
     * @param scannedObject
     * @return initialized scanned object
     * @throws Exception
     */
    public Object initializeInNewTx(int index, Object scannedObject) throws Exception {
        // For error test mode
        boolean initialized = true;

        // We are yet in one transaction, cf EventJob#execute
        Transaction originateTx = TransactionHelper.requireNewTransaction();
        try {
            Object initializedScanObject = initialize(index, scannedObject);

            // Error test mode
            if (ErrorTestMode.generateError(2)) {
                throw new ErrorTestModeException("Error on ScanUpdater#initialize");
            }

            return initializedScanObject;
        } catch (Exception e) {
            TransactionHelper.setTransactionRollbackOnly();

            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                initialized = false;
            }

            throw e;
        } finally {
            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                if (initialized) {
                    log.info("Initialized");
                } else {
                    log.info("NOT initialized");
                }
            }

            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.resumeTransaction(originateTx);
        }
    }


    /**
     * Initialize scanned object if necessary.
     * 
     * @param scannedObject
     * @return initialized scanned object
     * @throws Exception
     */
    public abstract Object initialize(int index, Object scannedObject) throws Exception;

    /**
     * Prepare object for next scan in transaction.
     * 
     * @param index
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public Object updateInNewTx(int index, Object scannedObject) throws Exception {
        // For error test mode
        boolean updated = true;

        // We are yet in one transaction, cf EventJob#execute
        Transaction originateTx = TransactionHelper.requireNewTransaction();
        try {
            Object updatedObject = update(index, scannedObject);

            // Error test mode
            if (ErrorTestMode.generateError(3)) {
                throw new ErrorTestModeException("Error on ScanUpdater#update");
            }

            return updatedObject;
        } catch (Exception e) {
            TransactionHelper.setTransactionRollbackOnly();

            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                updated = false;
            }

            throw e;
        } finally {
            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                if (updated) {
                    log.info("Updated");
                } else {
                    log.info("NOT updated");
                }
            }

            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.resumeTransaction(originateTx);
        }
    }

    /**
     * Prepare object for next scan.
     * 
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public abstract Object update(int index, Object scannedObject) throws Exception;

    /**
     * Prepare object for next scan if error occurs in transaction.
     * 
     * @param index
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public Object updateOnErrorInTx(int index, Object scannedObject) throws Exception {
        // For error test mode
        boolean updated = true;

        // We are in one transaction, cf EventJob#execute
        Transaction originateTx = TransactionHelper.requireNewTransaction();
        try {
            Object updatedObject = updateOnError(index, scannedObject);

            // Error test mode
            if (ErrorTestMode.generateError(5)) {
                throw new ErrorTestModeException("Error on ScanUpdater#updateOnError");
            }

            return updatedObject;
        } catch (Exception e) {
            TransactionHelper.setTransactionRollbackOnly();

            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                updated = false;
            }

            throw e;
        } finally {
            if (ErrorTestMode.isActivated() && log.isInfoEnabled()) {
                if (updated) {
                    log.info("Updated on mail error");
                } else {
                    log.info("NOT updated on mail error");
                }
            }

            TransactionHelper.commitOrRollbackTransaction();
            TransactionHelper.resumeTransaction(originateTx);
        }
    }

    /**
     * Prepare object for next scan if error occurs.
     * 
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public abstract Object updateOnError(int index, Object scannedObject) throws Exception;

}
