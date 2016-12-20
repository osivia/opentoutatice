/**
 * 
 */
package fr.toutatice.ecm.platform.automation.security;

import java.util.List;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;


/**
 * @author david
 *
 */
@Operation(id = AddACEs.ID)
public class AddACEs extends AbstractACEsOperation {

    public static final String ID = "Document.AddACEs";
    
    @Context
    protected CoreSession session;
    
    @Param(name = "acl", required = false, values = {ACL.INHERITED_ACL, ACL.LOCAL_ACL})
    protected String aclName = ACL.LOCAL_ACL;
    
    @Param(name = "aces", required = false)
    protected Properties aces;
    
    @Param(name = "blockInheritance", required = false)
    protected boolean blockInheritance = false;
    
    @OperationMethod
    public DocumentModel run(DocumentModel document) throws Exception {
        return super.execute(this.session, document, this.aclName, this.aces, this.blockInheritance);
    }

    /**
     * Add ACEs on ACL.
     * 
     * @param acl
     * @param aces
     * @return modifed ACL
     */
    @Override
    protected ACL modifyACEs(ACL acl, Properties aces) {
        List<ACE> acEsToAdd = ACEsOperationHelper.buildACEs(aces);

        for (ACE aceToAdd : acEsToAdd) {
            if (!acl.contains(aceToAdd)) {
                acl.add(aceToAdd);
            }
        }

        return acl;
    }
    
}
