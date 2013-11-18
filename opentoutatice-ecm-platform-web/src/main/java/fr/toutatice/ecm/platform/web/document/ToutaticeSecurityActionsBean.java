package fr.toutatice.ecm.platform.web.document;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.webapp.security.SecurityActionsBean;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Custom version of the security related methods.
 *
 * @author Marc Berhaut
 */
@Name("securityActions")
@Scope(CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeSecurityActionsBean extends SecurityActionsBean {

	private static final Log log = LogFactory.getLog(ToutaticeSecurityActionsBean.class);
	
	private static final long serialVersionUID = 8932929262490071180L;
	private static List<String> PUBLIC_USERS = null;
	private static String NX_GROUP_NAME = "Administrators";
	
	private Map<String, Boolean> publicUsersMap = new HashMap<String, Boolean>();
		
	/**
	 * Vérifie que l'utilisateur/groupe possède la permission de lecture ou écriture
	 * 
	 * @param acl la liste de droits à vérifier
	 * @param userName le nom de l'utilisateur/groupe à vérifier
	 */
	private void checkACLForUsername(ACL acl, String userName) {
		if (null != acl) {
			ACE[] aces = acl.getACEs();
			for (int i=0; i < aces.length; i++) {
				if (( SecurityConstants.READ.equals(aces[i].getPermission()) || SecurityConstants.WRITE.equals(aces[i].getPermission()))
				&& userName.equals(aces[i].getUsername())) {
					publicUsersMap.put(userName, aces[i].isGranted());
				}
			}
		}
	}
	
	/**
	 * Détermine si TOUS les éléments (utilisateurs/groups) du domaine public ont un droit de lecture (ou écriture) sur le document courant.
	 * 
	 * @return 'true' si c'est le test est positif. 'false' sinon.
	 * @throws ClientException
	 */
	public boolean isVisibleFromLargePublic() throws ClientException {
		boolean status = true;
		
		DocumentModel doc = navigationContext.getCurrentDocument();
		ACP acp = doc.getACP();
		publicUsersMap.clear();
		
		// Vérification des droits hérités
		ACL acl = acp.getACL(ACL.INHERITED_ACL);
		for (String userName : getPublicUsers()) {
			checkACLForUsername(acl, userName);
		}

		// Vérification des droits locaux: prioritaires sur les droits hérités
		for (String userName : getPublicUsers()) {
		acl = acp.getACL(ACL.LOCAL_ACL);
			checkACLForUsername(acl, userName);
		}
		
		// Vérifie pour chaque utilisateur et/ou groupe qui appartient au "domaine" public: le droit est actif 
		for (String userName : getPublicUsers()) {
			if ((null != userManager.getGroup(userName))
				|| (null != userManager.getUserModel(userName))) {
				if (!publicUsersMap.containsKey(userName) || (false == publicUsersMap.get(userName))) {
					status = false;
				}
			}
		}
		
		return status;
	}
	
	/**
	 * Poisitionne le droit de lecture pour chaque élément (utilisateur/groupe) du "domaine" public.
	 * @throws ClientException
	 */
	public void setVisibleFromLargePublic() throws ClientException {
		for (String userName : getPublicUsers()) {
			addPermission(userName, SecurityConstants.READ, true);
		}
	}

	/**
	 * Supprime le droit de lecture pour chaque élément (utilisateur/groupe) du "domaine" public.
	 * @throws ClientException
	 */
	public void setNotVisibleFromLargePublic() throws ClientException {
		for (String userName : getPublicUsers()) {
			addPermission(userName, SecurityConstants.READ, false);
		}
	}

	private List<String> getPublicUsers() throws ClientException {
		if (null == PUBLIC_USERS) {
			PUBLIC_USERS = new ArrayList<String>();
			String anonymous = userManager.getAnonymousUserId();
			if (StringUtils.isNotBlank(anonymous)) {
				PUBLIC_USERS.add(anonymous);
			}
			PUBLIC_USERS.add("members");
		}
		return PUBLIC_USERS;
	}

	@Override
    public String blockRightInheritance() throws ClientException {
		boolean doAddCurrentUSer = true;
        Boolean blockRightInheritance = getBlockRightInheritance();
        rebuildSecurityData();

        if (blockRightInheritance) {
            // Block
            securityData.addModifiablePrivilege(SecurityConstants.EVERYONE, SecurityConstants.EVERYTHING, false);
            
            // add user to avoid lock up
            Principal currentUser = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
            if (securityData.getCurrentDocumentUsers() != null
                    && !securityData.getCurrentDocumentUsers().contains(currentUser.getName())) {
                // add administrators to avoid LockUp
                List<String> adminGroups = userManager.getAdministratorsGroups();

				/**
				 * Mantis #3330: [Nuxeo] Le groupe "administrators" peut perdre ses droits 
				 * 
				 * !REMARQUE!
				 * Le groupe "Administrators" est ajouté ici explicitement plutôt que via la configuration (fichier "acaren-usermanager-config.xml")
				 * car l'ajout de "Administrators" au tag <administratorsGroup> impliquerait que les usagers nx... aurait tous les droits au même titre
				 * que le super administrateur. Ce qui doit être évité.
				 */
                DocumentModel nxGrp = userManager.getGroupModel(NX_GROUP_NAME);
                if (null != nxGrp) {
                	adminGroups.add(NX_GROUP_NAME);
                	for (String adminGroup : adminGroups) {
                		securityData.addModifiablePrivilege(adminGroup, SecurityConstants.EVERYTHING, true);
                		List<String> usersList = userManager.getUsersInGroup(NX_GROUP_NAME);
                		if (null != usersList && usersList.contains(currentUser.getName())) {
                			doAddCurrentUSer = false;
                		}
                	}
                }
				
				// ajouter l'utilisateur courant seulement s'il n'appartient pas aux groupes des administrateurs
				if (doAddCurrentUSer) {
					securityData.addModifiablePrivilege(currentUser.getName(), SecurityConstants.EVERYTHING, true);
				}
            }
        } else {
            securityData.removeModifiablePrivilege(SecurityConstants.EVERYONE,
                    SecurityConstants.EVERYTHING, false);
        }
        updateSecurityOnDocument();
        resetSecurityData();
        selectedEntries = null;
        return null;
    }
	
	public String updateSecurityOnDocument() throws ClientException {
		 String res = super.updateSecurityOnDocument();
		 updateACLProxy();
		 return res;
	 }
	 
	 private void updateACLProxy() throws ClientException{
		 DocumentModel currentDoc = navigationContext.getCurrentDocument();
		 DocumentModel proxyDoc = ToutaticeDocumentHelper.getProxy(documentManager, currentDoc, null);
		 if(proxyDoc == null){
			 log.info("Le document n'a pas de proxy");
		 }else{
			 log.info("Le document a un proxy; mise à jour des ACL sur ce proxy"); 
			//recuperation des ACls du document courant
				ACP srcACP = documentManager.getACP(currentDoc.getRef());
			//copie les ACLs
				ACP proxyACP = new ACPImpl();
				for (ACL acl : srcACP.getACLs()) {
					proxyACP.addACL(acl);
				}
			//sauvegarder les nouvelles acls sur le proxy
				documentManager.setACP(proxyDoc.getRef(), proxyACP, true);
		 }
	 }
}
