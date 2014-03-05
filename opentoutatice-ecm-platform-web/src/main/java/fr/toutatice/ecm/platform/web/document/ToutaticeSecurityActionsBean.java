package fr.toutatice.ecm.platform.web.document;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.webapp.security.SecurityActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Custom version of the security related methods.
 *
 * @author Marc Berhaut
 */
@Name("securityActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeSecurityActionsBean extends SecurityActionsBean {

	private static final Log log = LogFactory.getLog(ToutaticeSecurityActionsBean.class);
	
	private static final long serialVersionUID = 8932929262490071180L;

	@Override
	public String updateSecurityOnDocument() throws ClientException {
		 String res = super.updateSecurityOnDocument();
		 updateACLProxy();
		 return res;
	 }
	 
	 private void updateACLProxy() throws ClientException{
		 DocumentModel currentDoc = navigationContext.getCurrentDocument();
		 DocumentModel proxyDoc = ToutaticeDocumentHelper.getProxy(documentManager, currentDoc, null);
		 if (proxyDoc != null) {
			 log.debug("Le document a un proxy; mise à jour des ACL sur ce proxy"); 
			//recuperation des ACls du document courant
				ACP srcACP = documentManager.getACP(currentDoc.getRef());
			//copie les ACLs
				ACP proxyACP = new ACPImpl();
				for (ACL acl : srcACP.getACLs()) {
					proxyACP.addACL(acl);
				}
			//sauvegarder les nouvelles acls sur le proxy
				documentManager.setACP(proxyDoc.getRef(), proxyACP, true);
		 } else {
			 log.debug("Le document n'a pas de proxy");
		 }
	 }
	 
}
