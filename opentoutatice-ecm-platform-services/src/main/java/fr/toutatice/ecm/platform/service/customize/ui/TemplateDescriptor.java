/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("template")
public class TemplateDescriptor implements Serializable {

    private static final long serialVersionUID = -5423558848213576278L;
    
    @XNode("mode")
    String mode;
    
    public String getMode(){
        return mode;
    }

    @XNode("name")
    String name;
    
    public String getName(){
        return name;
    }
    
}
