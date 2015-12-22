/**
 * 
 */
package fr.toutatice.ecm.platform.core.persistence;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 * @author david
 *
 */
public class ToutaticeDocumentModelWriter extends DocumentModelWriter {
	
	private static final Log log = LogFactory.getLog(ToutaticeDocumentModelWriter.class);
	
	private UserManager um;

	public ToutaticeDocumentModelWriter(CoreSession session, String parentPath, int i) {
		super(session, parentPath, i);
	}
	
	public UserManager getUserManager(){
        if(um == null){
            um = (UserManager) Framework.getService(UserManager.class);
        }
        return um;
    }
	
	@Override
	protected DocumentModel createDocument(ExportedDocument xdoc, Path toPath)
			throws ClientException {

		DocumentModel createDocument = super.createDocument(xdoc, toPath);
		adaptACPs(createDocument);

		return createDocument;

	}
	
	private void adaptACPs(DocumentModel createDocument) {

        if (this.session.hasPermission(createDocument.getRef(), SecurityConstants.WRITE)) {

            ACP acp = super.session.getACP(createDocument.getRef());
            if (acp != null) {
                ACL inheritedAcl = acp.getACL(ACL.INHERITED_ACL);
                if (CollectionUtils.isNotEmpty(inheritedAcl)) {
                    adaptACEs(createDocument, super.session, acp, inheritedAcl);

                    ACL localAces = acp.getACL(ACL.LOCAL_ACL);
                    if (CollectionUtils.isNotEmpty(localAces)) {
                        adaptACEs(createDocument, super.session, acp, localAces);
                    }
                }

            }

        }

    }

    /**
     * @param createDocument
     * @param originalSession
     * @param acp
     * @param acl
     */
    private void adaptACEs(DocumentModel createDocument, CoreSession originalSession, ACP acp, ACL acl) {
        ACE[] aces = acl.getACEs();

        if (ArrayUtils.isNotEmpty(aces)) {
            
            NuxeoPrincipal principal = (NuxeoPrincipal) originalSession
                    .getPrincipal();
            ACE currentUserAce = new ACE(principal.getName(),
                    SecurityConstants.WRITE, true);
            
            List<ACE> acesList = new LinkedList<ACE>();
            for(ACE ace : aces){
                if(ACE.BLOCK.equals(ace)){
                    acesList.add(currentUserAce);
                    acesList.add(ACE.BLOCK);
                } else {
                    String username = ace.getUsername();
                    DocumentModel userModel = getUserManager().getUserModel(username);
                    
                    if(userModel != null){
                        acesList.add(ace);
                    }
                }
            }
            
            acl.setACEs(acesList.toArray(new ACE[acesList.size()]));
            acp.addACL(acl);
            this.session.setACP(createDocument.getRef(), acp,
                    true);

        }
    }

}
