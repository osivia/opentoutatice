/**
 * 
 */
package org.opentoutatice.ecm.scanner.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nuxeo.ecm.core.scheduler.ScheduleExtensionRegistry;
import org.opentoutatice.ecm.scanner.directive.DirectiveException;


/**
 * @author david
 *
 */
public class ScannerConfigurationRegistryImpl extends ScheduleExtensionRegistry implements ScannerConfigurationRegistry {
    
    /** Directive's parameters by event. */
    private Map<String, Map<String, Serializable>> directivesParamByEvent;
    
    /** Updater's parameterization. */
    private Map<String, ScanUpdaterConfiguration> updaterCfgByEvent;

    /**
     * Constructor.
     */
    public ScannerConfigurationRegistryImpl() {
        super();
        // Initialization
        this.directivesParamByEvent = new HashMap<String, Map<String, Serializable>>(1);
        this.updaterCfgByEvent = new HashMap<String, ScanUpdaterConfiguration>(1);
    }

    @Override
    public Entry<String, Serializable> getDirectiveParameterization(String eventId) throws DirectiveException {
        // Directive
        Entry<String, Serializable> paramEntry = null;

        // Get Directive parameterization: one (type, query) entry
        Map<String, Serializable> parameterization = this.directivesParamByEvent.get(eventId);
        // Coherence checks
        if (parameterization != null) {
            Set<Entry<String, Serializable>> paramEntrySet = parameterization.entrySet();

            if (paramEntrySet != null && paramEntrySet.size() == 1) {
                // (type, query) entry
                paramEntry = paramEntrySet.iterator().next();
                
            } else {
                throw new DirectiveException("More than one directive is configured for " + eventId);
            }
        }

        return paramEntry;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void registerDirectiveParameterization (ScannerConfiguration configuration) {
        this.directivesParamByEvent.put(configuration.getEventId(), configuration.getDirectiveParameterization());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScanUpdaterConfiguration getUpdaterParameterization(String eventId) {
        return this.updaterCfgByEvent.get(eventId);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void registerUpdaterParameterization(String eventId, ScanUpdaterConfiguration configuration) {
        this.updaterCfgByEvent.put(eventId, configuration);
    }

}
