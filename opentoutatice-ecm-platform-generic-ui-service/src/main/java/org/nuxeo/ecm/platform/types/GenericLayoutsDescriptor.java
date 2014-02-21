/**
 * 
 */
package org.nuxeo.ecm.platform.types;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * 
 * @author david
 *
 */
@XObject("layouts")
public class GenericLayoutsDescriptor implements Serializable {

    private static final long serialVersionUID = -4220461821847540652L;
    
    @XNode("@mode")
    String mode;

    public String getMode() {
        return mode;
    }
    
    @XNodeList(value = "layout", type = GenericLayoutDescriptor[].class, componentType = GenericLayoutDescriptor.class)
    GenericLayoutDescriptor[] layouts = new GenericLayoutDescriptor[0];
    
    public GenericLayoutDescriptor[] getLayoutsToInsert(){
        return layouts;
    }
}
