/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.core.helper;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.VersionModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.api.impl.VersionModelImpl;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;

public class ToutaticeDocumentHelper {

	private static final Log log = LogFactory.getLog(ToutaticeDocumentHelper.class);
	
    private static final String CONTENT_CATEGORY = "content";

	private ToutaticeDocumentHelper() {
		// static class, cannot be instantiated
	}

	/**
	 * Retourne la dernière version valide du document passé en paramètre.
	 * 
	 * @param document
	 * @param session
	 * @return
	 */
	public static DocumentModel getLatestDocumentVersion(DocumentModel document, CoreSession session) {
		DocumentModel latestDoc = null;

		if ((null != document) && (null != session)) {
			try {
				if (NuxeoStudioConst.CST_DOC_STATE_APPROVED.equals(document.getCurrentLifeCycleState())) {
					latestDoc = document;
				} else {
					List<DocumentModel> versionDocList;
					versionDocList = session.getVersions(document.getRef());
					Collections.sort(versionDocList, new DocumentVersionComparator());

					for (int i = 0; i < versionDocList.size(); i++) {
						DocumentModel versionDoc = versionDocList.get(i);
						if (NuxeoStudioConst.CST_DOC_STATE_APPROVED.equals(versionDoc.getCurrentLifeCycleState())) {
							latestDoc = versionDoc;
							break;
						}
					}
				}
			} catch (ClientException e) {
				log.debug("Failed to get the latest version of the document '" + document.getName() + "', error: " + e.getMessage());
			}
		}

		return latestDoc;
	}

	/**
	 * Retourne un objet de type VersionModel à partir du document (version)
	 * passé en paramètre.
	 * 
	 * @param version
	 * @return
	 * @throws DocumentException
	 */
	public static VersionModel getVersionModel(DocumentModel version) throws DocumentException {
		VersionModel versionModel = new VersionModelImpl();
		versionModel.setId(version.getId());
		versionModel.setLabel(version.getVersionLabel());
		return versionModel;
	}

	public static class DocumentVersionComparator implements Comparator<DocumentModel> {
		private DocumentVersionComparator() {
		}

		@Override
		public int compare(DocumentModel arg0, DocumentModel arg1) {
			int result = 0;
			if ((null != arg0) && (null != arg1)) {
				result = (isNewer(arg0, arg1) == true) ? 1 : -1;
			}

			return result;
		}

		/**
		 * Vérifie que le document de 'comparaison' à été modifié après le
		 * document de référence.
		 * 
		 * @param ref
		 *            le document 'de référence'
		 * @param comp
		 *            le document 'de comparaison'
		 * @return true si c'est le cas. false sinon.
		 */
		public static boolean isNewer(DocumentModel ref, DocumentModel comp) {
			boolean isNewer = false;

			try {
				Calendar ref_modified = (GregorianCalendar) ref.getPropertyValue("dc:modified");
				Calendar comp_modified = (GregorianCalendar) comp.getPropertyValue("dc:modified");
				isNewer = comp_modified.after(ref_modified);
			} catch (ClientException e) {
				log.debug("Failed to determine wich document is the latest modified");
			}

			return isNewer;
		}

		public static boolean isBigger(String v0, String v1) {
			String[] sV0 = v0.split("\\.");
			String[] sV1 = v1.split("\\.");
			int majorV0 = Integer.parseInt(sV0[0]);
			int minorV0 = Integer.parseInt(sV0[1]);
			int majorV1 = Integer.parseInt(sV1[0]);
			int minorV1 = Integer.parseInt(sV1[1]);

			return ((majorV0 > majorV1) || ((majorV0 == majorV1) && (minorV0 > minorV1)));
		}
	}

	/**
	 * Récupérer la liste des parents d'un document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param filter
	 *            un filtre pour filter les parents à convenance
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @return La liste des parents filtrée
	 */
	public static DocumentModelList getParentList(CoreSession session, DocumentModel document, Filter filter, boolean runInUnrestrictedMode) {
		return getParentList(session, document, filter, runInUnrestrictedMode, false);
	}

	/**
	 * Récupérer la liste des parents d'un document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param filter
	 *            un filtre pour filter les parents à convenance
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @param immediateOnly
	 *            est-ce qu'il faut retourner uniquement le parent immédiat
	 *            satisfaisant le filtre
	 * @return La liste des parents filtrée
	 */
	public static DocumentModelList getParentList(CoreSession session, DocumentModel document, Filter filter, boolean runInUnrestrictedMode, boolean immediateOnly) {
		return getParentList(session, document, filter, runInUnrestrictedMode, immediateOnly, false);
	}

	/**
	 * Récupérer la liste des parents d'un document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param filter
	 *            un filtre pour filter les parents à convenance
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @param immediateOnly
	 *            est-ce qu'il faut retourner uniquement le parent immédiat
	 *            satisfaisant le filtre
	 * @param thisInluded
	 *            est-ce que le document courant est examiné s'il est un folder
	 * @return La liste des parents filtrée
	 */
	public static DocumentModelList getParentList(CoreSession session, DocumentModel document, Filter filter, boolean runInUnrestrictedMode, boolean immediateOnly,
			boolean thisInluded) {
		DocumentModelList parent = null;

		try {
			UnrestrictedGetParentsListRunner runner = new UnrestrictedGetParentsListRunner(session, document, filter, immediateOnly, thisInluded);
			if (runInUnrestrictedMode) {
				runner.runUnrestricted();
			} else {
				runner.run();
			}
			parent = runner.getParentList();
		} catch (ClientException e) {
			log.error("Failed to get the parent for the current document, error: " + e.getMessage());
		}

		return parent;
	}

	/**
	 * Récupérer la liste des "spaces" parents d'un document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant? immediateOnly est-ce qu'il
	 *            faut retourner uniquement l'espace parent immédiat
	 * @return la liste des "spaces" parents d'un document
	 */
	public static DocumentModelList getParentSpaceList(CoreSession session, DocumentModel document, boolean runInUnrestrictedMode, boolean immediateOnly) {
		return getParentSpaceList(session, document, runInUnrestrictedMode, immediateOnly, false);
	}

	/**
	 * Récupérer la liste des "spaces" parents d'un document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @param immediateOnly
	 *            est-ce qu'il faut retourner uniquement l'espace parent
	 *            immédiat?
	 * @param thisIncluded
	 *            est-ce que le document courant doit être examiné?
	 * @return la liste des "spaces" parents d'un document
	 */
	public static DocumentModelList getParentSpaceList(CoreSession session, DocumentModel document, boolean runInUnrestrictedMode, boolean immediateOnly, boolean thisIncluded) {
		Filter filter = new Filter() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(DocumentModel document) {
				boolean res = false;
				if (isASpaceDocument(document)) {
					res = true;
				}
				return res;
			}
		};

		return ToutaticeDocumentHelper.getParentList(session, document, filter, runInUnrestrictedMode, immediateOnly, thisIncluded);
	}

	/**
	 * Récupérer la liste des "espaces de publication (locale)" parents d'un
	 * document.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @param immediateOnly
	 *            est-ce qu'il faut retourner uniquement l'espace de publication
	 *            parent immédiat
	 * @return la liste des "spaces" parents d'un document
	 */
	public static DocumentModelList getParentPublishSpaceList(CoreSession session, DocumentModel document, boolean runInUnrestrictedMode, boolean immediateOnly) {
		Filter filter = new Filter() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(DocumentModel document) {
				boolean status = false;

				try {
					status = ToutaticeDocumentHelper.isAPublicationSpaceDocument(document);
				} catch (Exception e) {
					log.error("Failed to filter the publish space documents, error: " + e.getMessage());
					status = false;
				}

				return status;
			}
		};

		return ToutaticeDocumentHelper.getParentList(session, document, filter, runInUnrestrictedMode, immediateOnly, true);
	}

	public static String[] filterLifeCycleStateDocuments(CoreSession session, String[] sectionIdList, List<String> acceptedStates, List<String> excludedStates) {
		List<String> filteredSectionsList = new ArrayList<String>();

		try {
			if (null != sectionIdList && 0 < sectionIdList.length) {
				Filter lcFilter = new LifeCycleFilter(acceptedStates, excludedStates);

				for (String sectionId : sectionIdList) {
					DocumentModel section = session.getDocument(new IdRef(sectionId));
					if (lcFilter.accept(section)) {
						filteredSectionsList.add(sectionId);
					}
				}
			}
		} catch (ClientException e) {
			log.error("Failed to filter the active sections, error: " + e.getMessage());
		}

		return filteredSectionsList.toArray(new String[filteredSectionsList.size()]);
	}

	/**
	 * Retourne le proxy d'un document s'il existe DANS UN MODE UNRESTRICTED. 
	 * Si une permission est précisée en paramètre elle sera contrôlée.
	 * 
	 * @param session la session utilisateur connecté courant
	 * @param document le document pour lequel la recherche est faite
	 * @return le proxy associé ou null si aucun proxy existe ou une exception
	 *         de sécurité si les droits sont insuffisants.
	 * @throws ClientException
	 */
	public static DocumentModel getProxy(CoreSession session, DocumentModel document, String permission) throws ClientException {
		return getProxy(session, document, permission, true);
	}

	/**
	 * Retourne le proxy d'un document s'il existe. 
	 * Si une permission est précisée en paramètre elle sera contrôlée.
	 * 
	 * @param session la session utilisateur connecté courant
	 * @param document le document pour lequel la recherche est faite
	 * @param unrestricted true si le proxy doit être récupéré en mode unrestricted. False s'il doit être récupéré avec la session utilisateur courante
	 * @return le proxy associé ou null si aucun proxy existe ou une exception
	 *         de sécurité si les droits sont insuffisants.
	 * @throws ClientException
	 */
	public static DocumentModel getProxy(CoreSession session, DocumentModel document, String permission, boolean unrestricted) throws ClientException {
		DocumentModel proxy = null;

		if (document.isProxy()) {
			proxy = document;
		} else {
			UnrestrictedGetProxyRunner runner = new UnrestrictedGetProxyRunner(session, document);
			if (unrestricted) {
				runner.runUnrestricted();
			} else {
				runner.run();
			}
			proxy = runner.getProxy();
		}

		if (null != proxy) {
			if (StringUtils.isNotBlank(permission) && !session.hasPermission(proxy.getRef(), permission)) {
				Principal principal = session.getPrincipal();
				throw new DocumentSecurityException("The user '" + principal.getName() + "' has not the permission '" + permission + "' on the proxy of doucment '"
						+ document.getTitle() + "'");
			}
		}

		return proxy;
	}

	/**
	 * Retourne la version d'un proxy de document s'il existe.
	 * 
	 * @param session
	 *            la session utilisateur connecté courant
	 * @param document
	 *            le document pour lequel la recherche est faite
	 * @return la version du proxy associé ou null si aucun proxy existe
	 * @throws ClientException
	 */
	public static String getProxyVersion(CoreSession session, DocumentModel document) throws ClientException {
		String proxyVersion = null;

		DocumentModel proxy = getProxy(session, document, null);
		if (null != proxy) {
			UnrestrictedGetProxyVersionLabelRunner runner = new UnrestrictedGetProxyVersionLabelRunner(session, proxy);
			runner.runUnrestricted();
			proxyVersion = runner.getVersionLabel();
		}

		return proxyVersion;
	}

	public static boolean isVisibleInPortal(DocumentModel doc, CoreSession session) throws ClientException {
		boolean res = false;

		// le document est en ligne ?
		res = (null != getProxy(session, doc, SecurityConstants.READ));

		if (!res) {
			// le document est dans un workspace ?
			DocumentModelList spaceDocsList = ToutaticeDocumentHelper.getParentSpaceList(session, doc, true, true, true);
			if(spaceDocsList!=null && !spaceDocsList.isEmpty()){
				DocumentModel space = spaceDocsList.get(0);
				res = ToutaticeDocumentHelper.isAWorkSpaceDocument(space);
			}
		}

		return res;
	}

	public static boolean isASpaceDocument(DocumentModel document) {
		return document.hasFacet(NuxeoStudioConst.CST_FACET_SPACE);
	}

	public static boolean isAPublicationSpaceDocument(DocumentModel document) {
		boolean status = false;

		if (document.hasFacet(NuxeoStudioConst.CST_DOC_FACET_TTC_PUBLISH_SPACE)) {
			status = true;
		}

		return status;
	}

	public static boolean isAWorkSpaceDocument(DocumentModel document) {
		boolean status = false;

		if (NuxeoStudioConst.CST_DOC_TYPE_WORKSPACE.equals(document.getType()) || NuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(document.getType())) {
			status = true;
		}

		return status;
	}

	private static class UnrestrictedGetProxyRunner extends UnrestrictedSessionRunner {
		private DocumentModel document;
		private DocumentModelList proxies;

		public UnrestrictedGetProxyRunner(CoreSession session, DocumentModel document) {
			super(session);
			this.proxies = null;
			this.document = document;
		}

		public DocumentModel getProxy() throws ClientException {
			return (null != this.proxies && 0 < this.proxies.size()) ? this.proxies.get(0) : null;
		}

		@Override
		public void run() throws ClientException {
			this.proxies = this.session.getProxies(document.getRef(), document.getParentRef());
		}

	}

	private static class UnrestrictedGetProxyVersionLabelRunner extends UnrestrictedSessionRunner {
		private DocumentModel proxy;
		private String versionLabel;

		public UnrestrictedGetProxyVersionLabelRunner(CoreSession session, DocumentModel proxy) {
			super(session);
			this.proxy = proxy;
			this.versionLabel = null;
		}

		@Override
		public void run() throws ClientException {
			String srcDocID = this.proxy.getSourceId();
			DocumentModel srcDoc = this.session.getDocument(new IdRef(srcDocID));
			this.versionLabel = srcDoc.getVersionLabel();
		}

		public String getVersionLabel() {
			return this.versionLabel;
		}

	}

	private static class UnrestrictedGetParentsListRunner extends UnrestrictedSessionRunner {
		private DocumentModel baseDoc;
		private DocumentModelList parentDocList;
		private Filter filter = null;
		private boolean immediateOnly;
		private boolean thisIncluded;

		public DocumentModelList getParentList() {
			return parentDocList;
		}

		protected UnrestrictedGetParentsListRunner(CoreSession session, DocumentModel document, Filter filter, boolean immediateOnly, boolean thisIncluded) {
			super(session);
			this.baseDoc = document;
			this.filter = filter;
			this.parentDocList = new DocumentModelListImpl();
			this.immediateOnly = immediateOnly;
			this.thisIncluded = thisIncluded;
		}

		@Override
		public void run() throws ClientException {

			if (this.thisIncluded && this.baseDoc.isFolder()) {
				if (null != this.filter) {
					if (this.filter.accept(this.baseDoc)) {
						this.parentDocList.add(this.baseDoc);
					}
				} else {
					this.parentDocList.add(this.baseDoc);
				}
			}

			DocumentRef[] parentsRefList = this.session.getParentDocumentRefs(this.baseDoc.getRef());
			if (null != parentsRefList && parentsRefList.length > 0) {
				for (DocumentRef parentsRef : parentsRefList) {
					DocumentModel parent = this.session.getDocument(parentsRef);
					if (null != this.filter) {
						if (this.filter.accept(parent)) {
							this.parentDocList.add(parent);
						}
					} else {
						this.parentDocList.add(parent);
					}

					if (this.immediateOnly && this.parentDocList.size() == 1) {
						break;
					}
				}
			}
		}
	}

	/**
	 * Récupérer la liste des documents similaires dans un dossier.
	 * 
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param filter
	 *            un filtre pour ajouter des critères de contrôle supplémentaires
	 *            
	 * @return le compte des documents similaires dans le dossier.
	 */
	public static int getSameDocsCount(CoreSession session, DocumentModel document, Filter filter) {
		int count = 0;

		try {
			UnrestrictedGetSameDocsListRunner runner = new UnrestrictedGetSameDocsListRunner(session, document, filter);
			runner.runUnrestricted();
			count = runner.getCount();
		} catch (ClientException e) {
			log.error("Failed to get the same document of one folder, error: " + e.getMessage());
		}

		return count;
	}

	private static class UnrestrictedGetSameDocsListRunner extends UnrestrictedSessionRunner {
		private int count;
		private Filter filter = null;
		private DocumentModel document;

		public int getCount() {
			return this.count;
		}

		protected UnrestrictedGetSameDocsListRunner(CoreSession session, DocumentModel document, Filter filter) {
			super(session);
			this.count = 0;
			this.filter = filter;
			this.document = document;
		}

		@Override
		public void run() throws ClientException {
			String docTitle = this.document.getTitle();
			DocumentModel docParent = this.session.getParentDocument(this.document.getRef());
			DocumentModelList rs = this.session.query("SELECT * FROM " + this.document.getType() + " WHERE ecm:parentId = '" + docParent.getId() + "' " +
					"AND ecm:mixinType != 'HiddenInNavigation' " +
					"AND ecm:isCheckedInVersion = 0 " +
					"AND dc:title LIKE '" + docTitle.replace("'", "\\'") + "%'",
					this.filter);

			this.count = rs.size();
		}
	}


	/**
	 * Copy les permissions dans l'ACL local en excluant les permissions passés en paramétre
	 * @param session
	 * @param doc 
	 * @param permissionsExclude permissions à exclure
	 * @throws ClientException
	 */
	public static void copyPermissionsInLocalACL(CoreSession session, DocumentModel doc,String permissionsExclude) throws ClientException{
		DocumentRef ref = doc.getRef(); 
		StringTokenizer st = new StringTokenizer(permissionsExclude,",");
		List<String> lstPerm = new ArrayList<String>(st.countTokens());
		while(st.hasMoreTokens()){
			lstPerm.add(st.nextToken());
		}  
		// nettoyer les acls local
		ACP acp = doc.getACP();
		acp.removeACL(ACL.LOCAL_ACL);
		session.setACP(ref, acp, true);

		//récupérer les acls du parent    	
		DocumentModel parent = session.getParentDocument(ref);
		ACP acpParent = parent.getACP();
		for (ACL acl : acpParent.getACLs()) {
			for (ACE ace : acl.getACEs()) {
				if (ace.isGranted() && !lstPerm.contains(ace.getPermission())) {
					// ajouter les permissions au document doc
					setACE(session, ref,ace);
				}
			}
		}            	
	}
	
	/**
	 * ajout une ace sur un document
	 * @param session
	 * @param ref
	 * @param ace
	 * @throws ClientException
	 */
	public static void setACE(CoreSession session, DocumentRef ref,ACE ace) throws ClientException {
		ACPImpl acp = new ACPImpl();
		ACLImpl acl = new ACLImpl(ACL.LOCAL_ACL);
		acp.addACL(acl);
		acl.add(ace);

		session.setACP(ref, acp, false);
	}

	/**
	 * @param document le document sur lequel porte le contrôle
	 * @param viewId l'identifiant de la vue
	 * @return true si le document porte la vue dont l'identifiant est passé en paramètre
	 */
	public static boolean hasView(DocumentModel document, String viewId) {
		boolean status = false;
		
		if (null != document) {
	        TypeInfo typeInfo = document.getAdapter(TypeInfo.class);
            String chosenView = typeInfo.getView(viewId);
            status = (chosenView != null);
		}
		
		return status;
	}
	
}
