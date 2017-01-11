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


/**
 * @author david
 *
 */
@XObject("updater")
public class ScanUpdaterConfiguration {

    /** Updater class. */
    @XNode("@class")
    private Class<?> updaterClass;

    /** Updater parameters. */
    @XNodeMap(value = "param", key = "@name", type = HashMap.class, componentType = String.class)
    private Map<String, Serializable> params = new HashMap<>(1);

    /**
     * @return the updaterClass
     */
    public Class<?> getUpdaterClass() {
        return updaterClass;
    }
    
    /**
     * @return the params
     */
    public Map<String, Serializable> getParams() {
        return params;
    }
    
}
