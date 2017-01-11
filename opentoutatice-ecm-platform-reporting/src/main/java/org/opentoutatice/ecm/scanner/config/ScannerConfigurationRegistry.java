/**
 * 
 */
package org.opentoutatice.ecm.scanner.config;

import java.io.Serializable;
import java.util.Map.Entry;

import org.opentoutatice.ecm.scanner.directive.DirectiveException;


/**
 * @author david
 *
 */
public interface ScannerConfigurationRegistry {
    
    /**
     * Gets directive's parameterization.
     * 
     * @param eventId
     * @return parameterization
     * @throws DirectiveException
     */
    Entry<String, Serializable> getDirectiveParameterization(String eventId) throws DirectiveException;
    
    /**
     * Register a directive's parameterization.
     * 
     * @param configuration
     * @throws DirectiveException
     */
    void registerDirectiveParameterization (ScannerConfiguration configuration) throws DirectiveException;
    
    /**
     * Gets updater's parameterization.
     * 
     * @param eventId
     * @return ScanUpdaterConfiguration
     * @throws Exception
     */
    ScanUpdaterConfiguration getUpdaterParameterization(String eventId) throws Exception;
    
    /**
     * Register updater's parameterization.
     * 
     * @param eventId
     * @param configuration
     * @throws Exception
     */
    void registerUpdaterParameterization(String eventId, ScanUpdaterConfiguration configuration) throws Exception;
    
    

}
