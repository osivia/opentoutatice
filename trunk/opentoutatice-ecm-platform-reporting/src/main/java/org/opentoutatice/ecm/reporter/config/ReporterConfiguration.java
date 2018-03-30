/**
 * 
 */
package org.opentoutatice.ecm.reporter.config;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("reporters")
public class ReporterConfiguration {

    /** Reporter class. */
    @XNodeMap(value = "reporter", key = "@caller", type = HashMap.class, componentType = Class.class)
    private Map<String, Class<?>> reporterClassesByCaller;


    /**
     * @return the reporter
     */
    public Map<String, Class<?>> getReporterClassesByCaller() {
        return reporterClassesByCaller;
    }

}
