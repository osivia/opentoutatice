/**
 * 
 */
package org.opentoutatice.ecm.scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.runtime.api.Framework;
import org.opentoutatice.ecm.reporting.ReportingRunner;
import org.opentoutatice.ecm.scanner.config.ScannerConfiguration;
import org.opentoutatice.ecm.scanner.config.ScannerConfigurationService;


/**
 * @author david
 *
 */
public class ScanningListener implements PostCommitFilteringEventListener {
    
    /** logger. */
    private static final Log log = LogFactory.getLog(ScanningListener.class);
    
    /** Configuration service. */
    private ScannerConfigurationService configurationService;
    
    /**
     * Filters on scan events.
     */
    @Override
    public boolean acceptEvent(Event event) {
        String eventCategory = (String) event.getContext().getProperty("eventCategory");
        return ScannerConfiguration.OTTC_SCAN_EVENT_CATEGORY.equals(eventCategory);
    }

    @Override
    public void handleEvent(EventBundle events) throws ClientException {
        this.configurationService = (ScannerConfigurationService) Framework.getService(ScannerConfigurationService.class);
        
        for(Event event : events){
            
            if(log.isDebugEnabled()){
                log.debug("Begin [" + event.getName() + "]");
            }
            
            try {
                
                // Run reporting
                ReportingRunner reporting = new ReportingRunner();
                reporting.run(event);
                
            } catch (Exception e) {
                if(log.isDebugEnabled()){
                    e.printStackTrace();
                }
                throw new ClientException(e);
            }
            
            if(log.isDebugEnabled()){
                log.debug("Ended [" + event.getName() + "]");
            }
        }
        
        

    }
    
//    /**
//     * Gets directive from configuration (set in event).
//     * 
//     * @param event
//     * @return directive
//     */
//    private Directive getDirective(ScannerConfigurationService configurationService, Event event) throws Exception {
//        // Directive
//        Directive directive = null;
//        // Context
//        EventContext eventContext = event.getContext();
//        
//        String scanEventName = (String) eventContext.getProperty("eventId");
//        // Check of coherent event 
//        if(StringUtils.equals(event.getName(), scanEventName)){
//            
//            Map<String, Serializable> directiveByEvent = configurationService.getDirectiveByEvent(scanEventName);
//            String directiveQuery = (String) directiveByEvent.get("nxqlQueryAndFetch");
//            
//            if(StringUtils.isNotBlank(directiveQuery)){
//                directive = new NxqlQueryAndFetchDirective(directiveQuery);
//            }
//        }
//        return directive;
//    }

}
