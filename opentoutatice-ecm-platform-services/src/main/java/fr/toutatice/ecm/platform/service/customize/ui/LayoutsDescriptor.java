/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("layouts")
public class LayoutsDescriptor implements Serializable {

    private static final long serialVersionUID = 4018050130582191720L;
    
    @XNodeList(value = "layout", type = LayoutDescriptor[].class, componentType = LayoutDescriptor.class)
    LayoutDescriptor[] layouts = new LayoutDescriptor[0];
    
    public LayoutDescriptor[] getLayouts(){
        return layouts;
    }
    
}
