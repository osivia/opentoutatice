/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import org.opentoutatice.ecm.scanner.directive.Directive;


/**
 * @author david
 *
 */
public interface Scanner {

    /**
     * Scans: gets objects according to a directive.
     * 
     * @param directive
     * @return scanned objects
     * @throws Exception
     */
    Iterable<?> scan(Directive directive) throws Exception;
    
    /**
     * Gets updater strategy.
     * 
     * @return ScanUpdaterStrategy
     */
    AbstractScanUpdater getUpdater();

}
