/**
 * 
 */
package fr.toutatice.ecm.platform.service.customize.types;

import java.io.Serializable;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.schema.DocumentTypeDescriptor;


/**
 * @author david
 *
 */
@XObject("basettc")
public class BaseTypeDescriptor implements Serializable {

    private static final long serialVersionUID = -905795010892961319L;
    
    @XNode("doctype")
    DocumentTypeDescriptor docTypeDescriptor;
    
    public DocumentTypeDescriptor getBaseDocTypeDescriptor(){
        return docTypeDescriptor;
    }

}
