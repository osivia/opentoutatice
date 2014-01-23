package org.nuxeo.ecm.platform.types;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * 
 * @author david
 *
 */
@XObject("layout")
public class GenericLayoutDescriptor implements Serializable {

    private static final long serialVersionUID = 2178639105074542595L;
    
    @XNode("@name")
    String name;

    public String getName() {
        return name;
    }
    
    @XNode("position")
    PositionLayoutDescriptor positionDescriptor;
    
    public PositionLayoutDescriptor getPositionLayoutDescriptor(){
        return positionDescriptor;
    }
    
    
    @XNodeList(value = "excludedTypes/type", type = String[].class, componentType = String.class)
    String[] excludedTypes = new String[0];
    
    public String[] getExcludedTypes(){
        return excludedTypes;
    }

}
