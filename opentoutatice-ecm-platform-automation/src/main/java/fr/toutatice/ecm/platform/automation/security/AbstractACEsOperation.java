/**
 * 
 */
package fr.toutatice.ecm.platform.automation.security;

import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;


/**
 * Abstract class to add or remove ACEs in inherited or local ACL.
 * It does not allow possibility to add / remove ACEs in inherited ACL 
 * if blockInhertance is set to true. 
 * 
 * @author david
 *
 */
public abstract class AbstractACEsOperation {

    /**
     * Constructor.
     */
    public AbstractACEsOperation() {
        super();
    }
    
    /**
     * Adds or remove ACEs from document.
     * 
     * @param document
     * @return document
     * @throws Exception
     */
    protected DocumentModel execute(CoreSession session, DocumentModel document, String aclName, Properties aces, 
            boolean blockInheritance) throws Exception{
        if (blockInheritance) {
            if (!ACL.INHERITED_ACL.equalsIgnoreCase(aclName)) {
                
                ACP acp = new ACPImpl();
                
                // Gets existing ACLs
                ACL existingAcl = document.getACP().getACL(aclName);
                
                if(existingAcl != null && !existingAcl.isEmpty()){
                    // Block inheritance
                    ACE blockInheritanceACe = ACEsOperationHelper.getBlockInheritanceACe();
                    if(existingAcl.contains(blockInheritanceACe)){
                        existingAcl.remove(ACEsOperationHelper.getBlockInheritanceACe());
                    }
                    
                    // Added or removed ACLs
                    existingAcl = modifyACEs(existingAcl, aces);
                    
                    // Block
                    existingAcl.add(ACEsOperationHelper.getBlockInheritanceACe());
                    
                    // To clear ACP cache
                    acp.addACL(existingAcl);
                } else {
                    // Default behavior when blocking inheritance
                    ACL localACL = ACEsOperationHelper.buildDefaultLocalACL(session, document);
                    
                    // Added or removed ACLs
                    localACL = modifyACEs(localACL, aces);
                    // Block inheritance
                    localACL.add(ACEsOperationHelper.getBlockInheritanceACe());
                    
                    // To clear ACP cache
                    acp.addACL(localACL);
                }
                
                // Save
                document.setACP(acp, true);
            }

        } else {
            // ACP update
            ACP acp = document.getACP();
            // ACL
            ACL acl = acp.getOrCreateACL(aclName);
            acl = modifyACEs(acl, aces);
            
            // Case of ACL removed
            if(acl == null){
                acp.removeACL(aclName);
            } else {
                // If bloc inheritance was set, remove it
                if(acl.contains(ACEsOperationHelper.getBlockInheritanceACe())){
                    acl.remove(ACEsOperationHelper.getBlockInheritanceACe());
                }
                
                acp.addACL(acl);
            }
            
            // Save
            document.setACP(acp, true);
        }

        return document;
    }
    
    /**
     * Removes all ACEs of ACL.
     * 
     * @param session
     * @param document
     * @param aclName
     * @return document
     */
    public DocumentModel execute(CoreSession session, DocumentModel document, String aclName) throws Exception {
        ACP acp = document.getACP();
        acp.removeACL(aclName);
        document.setACP(acp, true);
        return document;
    }
    
    /**
     * Adds or removes ACEs in given ACL.
     * 
     * @param acl
     * @param aces
     * @return modified acl
     */
    protected abstract ACL modifyACEs(ACL acl, Properties aces);

}
