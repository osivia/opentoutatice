/**
 * 
 */
package org.opentoutatice.ecm.scanner.config;

import java.io.Serializable;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.scheduler.SchedulerService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;
import org.opentoutatice.ecm.scanner.AbstractScanUpdater;
import org.opentoutatice.ecm.scanner.directive.Directive;
import org.opentoutatice.ecm.scanner.directive.DirectiveException;
import org.opentoutatice.ecm.scanner.directive.DirectiveFactory;
import org.opentoutatice.ecm.scanner.directive.DirectiveFactoryImpl;
import org.opentoutatice.ecm.scanner.directive.DirectiveType;


/**
 * @author david
 *
 */
public class ScannerConfigurationServiceImpl extends DefaultComponent implements ScannerConfigurationService {
    
    /** Scheduler service. */
    private SchedulerService schedulerService;
    
    /** Directives factory. */
    private DirectiveFactory directiveFactory;
    
    /** Configuration registry. */
    private ScannerConfigurationRegistryImpl configRegistry;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void activate(ComponentContext context) throws Exception{
        // Scheduler Service initialization
        if(this.schedulerService == null){
            this.schedulerService = Framework.getService(SchedulerService.class);
        }
        
        // Registry
        this.configRegistry = new ScannerConfigurationRegistryImpl();
        // Factory
        this.directiveFactory = new DirectiveFactoryImpl();
        
        super.activate(context);
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerExtension(Extension extension) throws Exception {
        // Only one extension point
        Object[] contributions = extension.getContributions();
        for(Object contribution : contributions){
            registerScannerConfiguration((ScannerConfiguration) contribution);
        }
    }
    
    // TODO: unregisterExtension
    
    /**
     * Register scanner configuration.
     * 
     * @param configuration
     * @throws Exception 
     */
    private void registerScannerConfiguration(ScannerConfiguration configuration) throws Exception {
        // Scheduler
        this.schedulerService.registerSchedule(configuration);
        // Directive
        this.configRegistry.registerDirectiveParameterization(configuration);
        // Updater parameterization
        this.configRegistry.registerUpdaterParameterization(configuration.getEventId(), configuration.getUpdaterParameterization());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Directive getDirective(Event event) throws DirectiveException {
        // Type, query
        Entry<String, Serializable> parameterization = this.configRegistry.getDirectiveParameterization(event.getName());
        DirectiveType type = DirectiveType.valueOf(parameterization.getKey());
        String query = (String) parameterization.getValue();
        
        // Creation
        return this.directiveFactory.create(type, query);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractScanUpdater getUpdater(Event event) throws Exception {
        // Parameterization
        ScanUpdaterConfiguration updaterCfg = this.configRegistry.getUpdaterParameterization(event.getName());
        
        // Instance
        AbstractScanUpdater updater = (AbstractScanUpdater) updaterCfg.getUpdaterClass().newInstance();
        // Parameters
        updater.setCfgParams(updaterCfg.getParams());
        
        return updater;
    }

}
