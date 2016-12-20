/**
 * 
 */
package fr.toutatice.ecm.platform.automation.security;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.CollectionUtils;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;


/**
 * Class to remove ACEs in given ACL.
 * Instead of removing ACE by ACE, it is possible to remove
 * all ACEs of given users 
 * or remove all ACEs of given ACL.
 * 
 * @author david
 *
 */
@Operation(id = RemoveACEs.ID)
public class RemoveACEs extends AbstractACEsOperation {
    
    public static final String ID = "Document.RemoveACEs";
    
    @Context
    protected CoreSession session;
    
    @Param(name = "acl", required = false, values = {ACL.INHERITED_ACL, ACL.LOCAL_ACL})
    protected String aclName = ACL.LOCAL_ACL;
    
    @Param(name = "all")
    protected boolean removeAll;
    
    @Param(name = "aces", required = false)
    protected Properties aces;
    
    @Param(name = "userNames", required = false)
    protected StringList userNames;
    
    @Param(name = "blockInheritance", required = false)
    protected boolean blockInheritance = false;
    
    @OperationMethod
    public DocumentModel run(DocumentModel document) throws Exception {
        if(this.removeAll){
            document = super.execute(session, document, this.aclName);
        } else {
            document = super.execute(this.session, document, this.aclName, this.aces, this.blockInheritance);
        }
        return document;
    }

    /**
     * Removes ACEs from ACL.
     * If parameter userNames id defined, all ACEs of users
     * will be removed.  
     * 
     * @param acl
     * @return modified ACL
     * 
     */
    @Override
    protected ACL modifyACEs(ACL acl, Properties aces) {
        // Case of remove all ACEs of given acl
        if(this.removeAll){
            return null;
        }
        
        ListIterator<ACE> aclIt = acl.listIterator();
        
        // ACEs to remove
        List<ACE> acEsToRemove = ACEsOperationHelper.buildACEs(aces);
        // Delete ACES by users
        boolean deleteByUsers = CollectionUtils.isNotEmpty(this.userNames);
        
        // Deletion of ACEs by users
        if(deleteByUsers){
            for(String userName : this.userNames){
                while(aclIt.hasNext()){
                    ACE docACe = aclIt.next();
                    if(docACe.getUsername().equals(userName)){
                        aclIt.remove();
                    }
                }
            }
        }
        
        // Deletion of given ACE
        for (ACE aceToRemove : acEsToRemove) {
            while (aclIt.hasNext()) {
                ACE docACe = aclIt.next();
                if (aceToRemove.equals(docACe)) {
                    // Default case
                    aclIt.remove();
                }
            }
        }

        return acl;
    }

}
