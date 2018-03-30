/**
 * 
 */
package org.opentoutatice.ecm.reporter.config;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.runtime.model.DefaultComponent;
import org.nuxeo.runtime.model.Extension;
import org.opentoutatice.ecm.reporter.Reporter;


/**
 * @author david
 *
 */
public class ReporterConfigurationServiceImpl extends DefaultComponent implements ReporterConfigurationService {
    
    /** Reporter's class by callerId. */
    private Map<String, Class<?>> reportersByCaller;
    
    @Override
    public void activate(org.nuxeo.runtime.model.ComponentContext context) throws Exception {
        this.reportersByCaller = new HashMap<String, Class<?>>();
    }
    
    @Override
    public void registerExtension(Extension extension) throws Exception {
        // Contributions
        Object[] contributions = extension.getContributions();
        
        if(contributions != null){
            // Register
            for (Object contribution : contributions) {
                ReporterConfiguration cfg = (ReporterConfiguration) contribution;
                this.reportersByCaller.putAll(cfg.getReporterClassesByCaller());
            }
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter getReporter(String caller) throws InstantiationException, IllegalAccessException {
        // Class reporter
        Class<?> reporterClass = this.reportersByCaller.get(caller);
        return (Reporter) reporterClass.newInstance();
    }


}
