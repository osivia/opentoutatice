/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import java.io.Serializable;
import java.util.Map;


/**
 * @author david
 *
 */
public abstract class AbstractScanUpdater {
    
    /** Updater parameters. */
    private Map<String, Serializable> cfgParams;
    
    /**
     * Default constructor.
     */
    public AbstractScanUpdater(){
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
     * Filters object.
     * 
     * @param index
     * @param scannedObject
     * @return boolean
     * @throws Exception
     */
    public abstract boolean accept(int index, Object scannedObject) throws Exception;
    
    /**
     * Initialize scanned object if necessary.
     * 
     * @param scannedObject
     * @return
     * @throws Exception
     */
    public abstract Object initialize(int index, Object scannedObject) throws Exception;
    
    /**
     * Prepare object for next scan.
     * 
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public abstract Object update(int index, Object scannedObject) throws Exception;
    
    /**
     * Prepare object for next scan if error occurs.
     * 
     * @param scannedObject
     * @return ScannedObject
     * @throws Exception
     */
    public abstract Object updateOnError(int index, Object scannedObject) throws Exception;
    
}
