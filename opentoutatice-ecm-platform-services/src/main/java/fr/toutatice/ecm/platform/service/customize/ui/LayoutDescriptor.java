/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.ui;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;


/**
 * @author david
 *
 */
@XObject("layout")
public class LayoutDescriptor implements Serializable {

    private static final long serialVersionUID = 6869042352681240828L;
    
    @XNode("@name")
    String name;

    public String getName() {
        return name;
    }
    
    @XNodeList(value = "templates/template", type = TemplateDescriptor[].class, componentType = TemplateDescriptor.class)
    TemplateDescriptor[] templates = new TemplateDescriptor[0];
    
    public TemplateDescriptor[] getTemplates(){
        return templates;
    }

}
