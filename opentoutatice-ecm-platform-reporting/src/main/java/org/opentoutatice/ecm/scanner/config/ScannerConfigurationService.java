/**
 * 
 */
package org.opentoutatice.ecm.scanner.config;

import org.nuxeo.ecm.core.event.Event;
import org.opentoutatice.ecm.scanner.AbstractScanUpdater;
import org.opentoutatice.ecm.scanner.directive.Directive;
import org.opentoutatice.ecm.scanner.directive.DirectiveException;


/**
 * @author david
 *
 */
public interface ScannerConfigurationService {
    
    /**
     * Gets the directive associated with the event fired by the scheduler.
     * 
     * @param event
     * @return Directive
     * @throws DirectiveException
     */
    Directive getDirective(Event event) throws DirectiveException;
    
    /**
     * Gets updater's class.
     * 
     * @param event
     * @return Class<?>
     * @throws Exception
     */
    AbstractScanUpdater getUpdater(Event event) throws Exception;

}
