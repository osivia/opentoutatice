/**
 * 
 */
package fr.toutatice.ecm.platform.automation.security;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.platform.usermanager.UserManager;


/**
 * @author david
 *
 */
@Operation(id = GetDocumentACLs.ID, category = Constants.CAT_DOCUMENT, label = "Gets document's ACLs", description = "Gets document's ACLs")
public class GetDocumentACLs {
    
    /** Operation's id. */
    public final static String ID = "Document.GetACLs";
    
    /** Session. */
    @Context
    protected CoreSession session;
    
    /** User Manager. */
    @Context
    protected UserManager userManager;
    
    /**
     * @param document
     * @return ACEs of document as List.
     * @throws NuxeoException
     */
    @OperationMethod
    public Object run(DocumentModel document) throws NuxeoException {
        JSONObject allACLs = new JSONObject();
        
        ACP acp = session.getACP(document.getRef());
        if (acp != null) {
            ACL[] acLs = acp.getACLs();
            if (ArrayUtils.isNotEmpty(acLs)) {
                JSONArray inheritedACLs = new JSONArray();
                JSONArray localACLs = new JSONArray();
                for (ACL acl : acLs) {
                    if (ACL.INHERITED_ACL.equals(acl.getName())) {
                        extractNSetACEs(inheritedACLs, acl);
                    } else if (ACL.LOCAL_ACL.equals(acl.getName())) {
                        extractNSetACEs(localACLs, acl);
                    }
                }
                allACLs.element(ACL.INHERITED_ACL, inheritedACLs);
                allACLs.element(ACL.LOCAL_ACL, localACLs);
            }
        }
        
        return new StringBlob(allACLs.toString(), "application/json");
    }

    /**
     * Extract ACEs of given ACL and set them in JSONArray.
     * 
     * @param jsonACEs
     * @param acl
     */
    protected void extractNSetACEs(JSONArray jsonACEs, ACL acl) {
        ACE[] acEs = acl.getACEs();
        if (ArrayUtils.isNotEmpty(acEs)) {
            List<String> groupIds = userManager.getGroupIds();
            for (ACE ace : acEs) {
                jsonACEs.add(convert(ace, groupIds));
            }
        }
    }
    
    /**
     * Converts ACE to JSOObject.
     * 
     * @param ace
     * @param groupIds
     * @return ACE as JSONObject
     */
    protected JSONObject convert(ACE ace, List<String> groupIds){
        JSONObject aceEntry = new JSONObject();
        aceEntry.element("username", ace.getUsername());
        aceEntry.element("permission", ace.getPermission());
        aceEntry.element("isGranted", ace.isGranted());
        
        if(CollectionUtils.isNotEmpty(groupIds)){
            aceEntry.element("isGroup", groupIds.contains(ace.getUsername()));
        } else {
            aceEntry.element("isGroup", false);
        }
        
        return aceEntry;
    }

}
