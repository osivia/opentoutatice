/**
 * 
 */
package org.opentoutatice.ecm.reporter.config;

import org.opentoutatice.ecm.reporter.Reporter;


/**
 * @author david
 *
 */
public interface ReporterConfigurationService {
    
    /**
     * Gets reporter. 
     * 
     * @return Reporter
     */
    Reporter getReporter(String caller) throws InstantiationException, IllegalAccessException;

}
