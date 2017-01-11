/**
 * 
 */
package org.opentoutatice.ecm.scanner.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.scheduler.ScheduleImpl;


/**
 * Specialization of Schedule
 * adding a NXQL query to tell which elements must be scanned or (scheduled).
 * 
 * @author david
 *
 */
@XObject("schedule")
public class ScannerConfiguration extends ScheduleImpl {

    private static final long serialVersionUID = -4666732940403026414L;

    /** Event category. */
    public static final String OTTC_SCAN_EVENT_CATEGORY = "ottcScan";
    
    // FIXME: default key: nxqlQuery
    @XNodeMap(value = "directive", key = "@type", type = HashMap.class, componentType = String.class)
    private Map<String, Serializable> directiveParameterization;
    
    @XNode("updater")
    private ScanUpdaterConfiguration updaterParameterization;
    
    @XNode("eventCategory")
    private final String eventCategory = OTTC_SCAN_EVENT_CATEGORY;
    
    /**
     * Getter for directive's query.
     * 
     * @return directive's query
     */
    public Map<String, Serializable> getDirectiveParameterization(){
        return this.directiveParameterization;
    }
    
    /**
     * @return the updaterParameterization
     */
    public ScanUpdaterConfiguration getUpdaterParameterization() {
        return updaterParameterization;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventCategory(){
        return this.eventCategory;
    }
    
}
