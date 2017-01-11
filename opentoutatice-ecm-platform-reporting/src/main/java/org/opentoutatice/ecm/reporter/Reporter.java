/**
 * 
 */
package org.opentoutatice.ecm.reporter;




/**
 * @author david
 *
 */
public interface Reporter {
    
    /**
     * Adapts input data to data usable by reporter.
     * 
     * @param inputData
     * @return Object
     * @throws Exception
     */
    Object adapt(Object inputData) throws Exception;
    
    /**
     * Builds report.
     * 
     * @param data
     * @return
     */
    Object build(Object data) throws Exception;
    
    /**
     * Send report.
     * 
     * @param report
     * @throws Exception
     */
    void send(Object report) throws Exception;

}
