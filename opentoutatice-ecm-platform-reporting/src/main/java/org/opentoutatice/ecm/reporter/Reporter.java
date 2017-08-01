/**
 * 
 */
package org.opentoutatice.ecm.reporter;

import javax.mail.MessagingException;

import org.opentoutatice.ecm.reporting.test.mode.ErrorTestModeException;



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
     * @param index
     * @param data
     * @return Object
     */
    Object build(int index, Object data) throws Exception;
    
    /**
     * Send report.
     * 
     * @param report
     * @throws Exception
     */
    void send(Object report) throws MessagingException, ErrorTestModeException;

}
