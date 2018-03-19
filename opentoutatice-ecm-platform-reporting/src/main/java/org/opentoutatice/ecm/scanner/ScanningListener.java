/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.ecm.reporting.ReportingRunner;
import org.opentoutatice.ecm.scanner.config.ScannerConfiguration;
import org.opentoutatice.ecm.scanner.config.ScannerConfigurationService;


/**
 * @author david
 *
 */
public class ScanningListener implements EventListener {

    /** logger. */
    private static final Log log = LogFactory.getLog(ScanningListener.class);

    /** Configuration service. */
    private ScannerConfigurationService configurationService;

    @Override
    public void handleEvent(Event event) throws NuxeoException {
        // Robustness
        if (StringUtils.equals(ScannerConfiguration.OTTC_SCAN_EVENT, event.getName())) {
            // Configuration service
            this.configurationService = (ScannerConfigurationService) Framework.getService(ScannerConfigurationService.class);

            if (this.configurationService == null) {
                log.error("No ScannerConfigurationService defined");
            } else {
                // Treatment
                long begin = System.currentTimeMillis();
                if (log.isDebugEnabled()) {
                    log.debug("Begin [" + event.getName() + "]");
                }

                try {

                    // Run reporting
                    ReportingRunner reporting = new ReportingRunner();
                    reporting.run(event);

                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        e.printStackTrace();
                    }
                    throw new NuxeoException(e);
                }

                long end = System.currentTimeMillis();
                long duration = end - begin;
                if (log.isDebugEnabled()) {
                    log.debug("Ended [" + event.getName() + "]: " + duration + " ms");
                }
            }
        }
    }

    // /**
    // * Gets directive from configuration (set in event).
    // *
    // * @param event
    // * @return directive
    // */
    // private Directive getDirective(ScannerConfigurationService configurationService, Event event) throws Exception {
    // // Directive
    // Directive directive = null;
    // // Context
    // EventContext eventContext = event.getContext();
    //
    // String scanEventName = (String) eventContext.getProperty("eventId");
    // // Check of coherent event
    // if(StringUtils.equals(event.getName(), scanEventName)){
    //
    // Map<String, Serializable> directiveByEvent = configurationService.getDirectiveByEvent(scanEventName);
    // String directiveQuery = (String) directiveByEvent.get("nxqlQueryAndFetch");
    //
    // if(StringUtils.isNotBlank(directiveQuery)){
    // directive = new NxqlQueryAndFetchDirective(directiveQuery);
    // }
    // }
    // return directive;
    // }

}
