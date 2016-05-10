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
 *   dchevrier
 *   lbillon
 *   sjahier 
 *    
 */
package fr.toutatice.ecm.platform.core.helper;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationNotFoundException;
import org.nuxeo.ecm.automation.OperationType;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.impl.InvokableMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.VersionModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.api.impl.VersionModelImpl;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.core.schema.types.Type;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.types.adapter.TypeInfo;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

public class ToutaticeDocumentHelper {

	private static final Log log = LogFactory.getLog(ToutaticeDocumentHelper.class);
	private static final String MEDIALIB = "MediaLibrary";
	
	private ToutaticeDocumentHelper() {
		// static class, cannot be instantiated
	}
	
	/**
	 * @param session
	 * @param id
	 * @return a document fetched with unrestricted session.
	 */
	public static DocumentModel getUnrestrictedDocument(CoreSession session, String id) throws ClientException {
	    
	    GetUnrestrictedDocument getter = new GetUnrestrictedDocument(session, id);
	    getter.runUnrestricted();
	    return getter.getDocument();
	    
	}
	
	/**
     * Allows to get a document in an unrestricted way.
	 */
	public static class GetUnrestrictedDocument extends UnrestrictedSessionRunner {

	    private String id;
	    private DocumentModel document;
	    
	    public DocumentModel getDocument(){
	        return this.document;
	    }
        
	    public GetUnrestrictedDocument(CoreSession session, String id){
            super(session);
            this.id = id;
        }
        
        @Override
        public void run() throws ClientException {
            if(StringUtils.isNotBlank(this.id)){
                DocumentRef ref = null;
                if(!StringUtils.contains(this.id, "/")){
                    ref = new IdRef(this.id);
                } else {
                    ref = new PathRef(this.id);
                }
                this.document = this.session.getDocument(ref);
            }
            
        }
	}

	/**
     * Save a document in an silent unrestricted or not way:
     * EventService and VersioningService are bypassed.
     */
    public static void saveDocumentSilently(CoreSession session, DocumentModel document, boolean unrestricted) {
        SilentSave save = new SilentSave(session, document);
        save.silentRun(unrestricted, ToutaticeGlobalConst.EVENT_N_VERSIONING_FILTERD_SERVICE);
    }
    
    /**
     * Save a document bypassing given services.
     * 
     * @param session
     * @param document
     * @param services
     * @param unrestricted
     */
    public static void saveDocumentSilently(CoreSession session, DocumentModel document, List<Class<?>> services, boolean unrestricted) {
        SilentSave save = new SilentSave(session, document);
        save.silentRun(unrestricted, services);
    }
    
    /**
     * Save document with no versioning.
     */
    public static void saveDocumentWithNoVersioning(CoreSession session, DocumentModel document, boolean unrestricted){
        SilentSave save = new SilentSave(session, document);
        save.silentRun(unrestricted, ToutaticeGlobalConst.VERSIONING_FILTERD_SERVICE);
    }

    /**
     * Save a document in an silent way.
     */
    public static class SilentSave extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;

        protected SilentSave(CoreSession session, DocumentModel document) {
            super(session);
            this.document = document;
        }

        @Override
        public void run() throws ClientException {
            this.session.saveDocument(this.document);
        }

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
				if (ToutaticeNuxeoStudioConst.CST_DOC_STATE_APPROVED.equals(document.getCurrentLifeCycleState())) {
					latestDoc = document;
				} else {
					List<DocumentModel> versionDocList;
					versionDocList = session.getVersions(document.getRef());
					Collections.sort(versionDocList, new DocumentVersionComparator());

					for (int i = 0; i < versionDocList.size(); i++) {
						DocumentModel versionDoc = versionDocList.get(i);
						if (ToutaticeNuxeoStudioConst.CST_DOC_STATE_APPROVED.equals(versionDoc.getCurrentLifeCycleState())) {
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
	public static DocumentModelList getParentList(CoreSession session, DocumentModel document, Filter filter, boolean runInUnrestrictedMode, boolean immediateOnly, boolean thisInluded) {
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
			log.warn("Failed to get the parent for the current document, error: " + e.getMessage());
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
	 * @param session la session courante de l'utilisateur
	 * @param document le document pour lequel il faut rechercher le parent
	 * @param lstXpaths les xpaths des propriétés qu'il faut retourner
	 * @param filter filtre déterminant le parent à consulter
	 * @param runInUnrestrictedMode opération doit-être exécuter en mode unrestricted	  
	 * @param thisIncluded le document courant est examiné 
	 * @return
	 *    une map de <xpath, property> du document parent 
	 */
	public static Map<String,Property> getPropertiesParentDoc(CoreSession session, DocumentModel document,List<String> lstXpaths,Filter filter, boolean runInUnrestrictedMode, boolean thisIncluded){
		Map<String,Property> mapPpty = null;
		try {
			GetParentPropertiesRunner runner = new GetParentPropertiesRunner(session, document, lstXpaths, filter,runInUnrestrictedMode, thisIncluded);
			if (runInUnrestrictedMode) {
				runner.runUnrestricted();
			} else {
				runner.run();
			}
			mapPpty = runner.getProperties();
		} catch (ClientException e) {
			log.warn("Failed to get the parent for the current document, error: " + e.getMessage());
		}		
		
		return mapPpty;
	}

	/**
	 * 
	 * @param session
	 * @param document
	 * @return
	 * @throws ClientException
	 * @throws PropertyException
	 */
	public static String getSpaceID(CoreSession session, DocumentModel document, boolean runInUnrestrictedMode) throws ClientException, PropertyException {
		String spaceId = ""; // le document courant n'appartient pas à un space

		// si UserWorspace => spaceId = dc:title (conversion en minuscule afin de pouvoir utiliser l'indexation sur cette méta-donnée)
		if (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(document.getType())) {
			spaceId = document.getTitle().toLowerCase();
		} else {
			// sinon récupérer la liste des spaceParents
			DocumentModelList spaceParentList = getParentSpaceList(session, document, runInUnrestrictedMode, true);

			if (spaceParentList != null && !spaceParentList.isEmpty()) {
				// prendre le 1er parent de type space rencontré
				DocumentModel space = spaceParentList.get(0);

				if (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(space.getType())) {
					// si le type de ce space est UserWorkspace => spaceID = dc:title
					spaceId = space.getTitle().toLowerCase();
				} else {
					// sinon spaceID = space.getId
					spaceId = space.getId();
				}
			}
		}
		
		return spaceId;
	}

	/**
	 * @param session
	 *            la session courante de l'utilisateur
	 * @param document
	 *            le document pour lequel il faut rechercher les parents
	 * @param runInUnrestrictedMode
	 *            Est-ce que cette opération de recherche des parents doit être
	 *            exécutée en mode unrestricted (session System) ou bien avec la
	 *            session de l'utilisateur courant?
	 * @return le domain parent du document courant
	 * @throws ClientException
	 */
	public static DocumentModel getDomain(CoreSession session, DocumentModel document, boolean runInUnrestrictedMode) throws ClientException {
		DocumentModel domain = null;

		// sinon récupérer la liste des spaceParents
		@SuppressWarnings("serial")
		DocumentModelList DomainList = getParentList(session, document, new Filter() {

			@Override
			public boolean accept(DocumentModel docModel) {
				return ToutaticeNuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(docModel.getType());
			}
		}, runInUnrestrictedMode, true, true);

		if (null != DomainList && !DomainList.isEmpty()) {
			domain = DomainList.get(0);
		} 

		return domain;
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
	 * Retourne le proxy d'un document s'il existe. Recherche réalisée en MODE UNRESTRICTED. 
	 * La recherche est effectuée uniquement sur le périmètre du container direct du document (local).
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
	 * La recherche est effectuée uniquement sur le périmètre du container direct du document (local).
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
		
		DocumentModelList proxies = getProxies(session, document, ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE.LOCAL, permission, unrestricted);
		if (null != proxies && 0 < proxies.size()) {
			proxy = proxies.get(0);
			if (StringUtils.isNotBlank(permission) && !session.hasPermission(proxy.getRef(), permission)) {
				Principal principal = session.getPrincipal();
				throw new DocumentSecurityException("The user '" + principal.getName() + "' has not the permission '" + permission + "' on the proxy of document '"
						+ document.getTitle() + "'");
			}
		}
		
		return proxy;
	}
	
	/**
	 * Retourne les proxies d'un document s'ils existent. 
	 * 
	 * @param session la session utilisateur connecté courant
	 * @param document le document pour lequel la recherche est faite
	 * @param scope le périmètre de la recherche: enumeration {@link ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE}
	 * @param unrestricted true si le proxy doit être récupéré en mode unrestricted. False s'il doit être récupéré avec la session utilisateur courante
	 * @return le proxy associé ou null si aucun proxy existe ou une exception
	 *         de sécurité si les droits sont insuffisants.
	 * @throws ClientException
	 */
	public static DocumentModelList getProxies(CoreSession session, DocumentModel document, ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE scope, String permission, boolean unrestricted) throws ClientException {
		DocumentModelList proxies = null;

		if (null != document) {
			if (document.isProxy()) {
				proxies = new DocumentModelListImpl();
				proxies.add(document);
			} else {
				UnrestrictedGetProxyRunner runner = new UnrestrictedGetProxyRunner(session, document, scope);
				if (unrestricted) {
					runner.runUnrestricted();
				} else {
					runner.run();
				}
				proxies = runner.getProxies();
			}			
		}

		return proxies;
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
            // le document est dans un workspace ou est en attente de publication dans un PortalSite
			DocumentModelList spaceDocsList = ToutaticeDocumentHelper.getParentSpaceList(session, doc, true, true, true);
			if(spaceDocsList!=null && !spaceDocsList.isEmpty()){
				DocumentModel space = spaceDocsList.get(0);
                res = ToutaticeDocumentHelper.isAWorkSpaceDocument(space) || ToutaticeDocumentHelper.isAPublicationSpaceDocument(space);
			}
		}

		return res;
	}

	public static boolean isASpaceDocument(DocumentModel document) {
		return document.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SPACE);
	}
	
	public static boolean isASuperSpaceDocument(DocumentModel document) {
		return document.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SUPERSPACE);
	}

	public static boolean isAPublicationSpaceDocument(DocumentModel document) {
		boolean status = false;

		if (document.hasFacet(ToutaticeNuxeoStudioConst.CST_DOC_FACET_TTC_PUBLISH_SPACE)) {
			status = true;
		}

		return status;
	}
	
	/**
	 * 
	 * @param document
	 * @return true if document is Workspace or extends it.
	 */
	// FIXME: UserWorkspace test to move in AcRennes.
	public static boolean isAWorkSpaceDocument(DocumentModel document) {
	    return ToutaticeNuxeoStudioConst.CST_DOC_TYPE_WORKSPACE.equals(document.getType()) || ToutaticeNuxeoStudioConst.CST_DOC_TYPE_USER_WORKSPACE.equals(document.getType())
	            || isSubTypeOf(document, ToutaticeNuxeoStudioConst.CST_DOC_TYPE_WORKSPACE);
	}
	
	/**
	 * 
	 * @param document
	 * @param type
	 * @return true if document extends a document with givent type.
	 */
	public static boolean isSubTypeOf(DocumentModel document, String type){
	    DocumentType documentType = document.getDocumentType();
	    if(documentType != null){
            Type superType = documentType.getSuperType();
            if(superType != null){
                return StringUtils.equals(type, superType.getName());
            }
	    }
       
        return false;
	}
	
	/**
	 * 
	 * @param session
	 * @param document
	 * @return true if document is in publish space.
	 */
	public static boolean isInPublishSpace(CoreSession session, DocumentModel document){
	    DocumentModelList parentPublishSpaceList = getParentPublishSpaceList(session, document, true, true);
	    return CollectionUtils.isNotEmpty(parentPublishSpaceList);
	}
	
	@SuppressWarnings("unused")
	private static class UnrestrictedGetProxyRunner extends UnrestrictedSessionRunner {
		private DocumentModel document;
		private DocumentModelList proxies;
		private ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE scope;

		public UnrestrictedGetProxyRunner(CoreSession session, DocumentModel document) {
			this(session, document, ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE.LOCAL);
		}

		public UnrestrictedGetProxyRunner(CoreSession session, DocumentModel document, ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE scope) {
			super(session);
			this.proxies = null;
			this.document = document;
			this.scope = scope;
		}

		public DocumentModel getProxy() throws ClientException {
			return (null != this.proxies && 0 < this.proxies.size()) ? this.proxies.get(0) : null;
		}

		public DocumentModelList getProxies() throws ClientException {
			return (null != this.proxies && 0 < this.proxies.size()) ? this.proxies : null;
		}
		
		@Override
		public void run() throws ClientException {
			if (null == scope || ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE.GLOBAL.equals(scope)) {
				// lookup all proxies of the document (wherever they are placed in the repository) 
				this.proxies = this.session.getProxies(document.getRef(), null);
			} else {
				// lookup only the proxies of the document placed in the parent folder
				this.proxies = this.session.getProxies(document.getRef(), document.getParentRef());
			}
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
			// For compatibility with content views managed by ES (JsonDocumentModelReader#getDocumentModel
			// has sid null when fetchFromEs is true
			if(srcDocID == null){
			    DocumentModel sourceDocument = this.session.getSourceDocument(this.proxy.getRef());
			    srcDocID = sourceDocument.getId();
			}
			DocumentModel srcDoc = this.session.getDocument(new IdRef(srcDocID));
			this.versionLabel = srcDoc.getVersionLabel();
		}

		public String getVersionLabel() {
			return this.versionLabel;
		}

	}
	
	private static class GetParentPropertiesRunner extends UnrestrictedSessionRunner{

		private DocumentModel doc;
		private Filter filter;		
		private boolean included;
		private boolean runInUnrestrictedMode;
		private List<String> lstxpath;
		private Map<String,Property> mapPpties;
		
		public GetParentPropertiesRunner(CoreSession session, DocumentModel document,List<String> lstXpaths,Filter filter,boolean runInUnrestrictedMode, boolean included){
			super(session);
			this.doc = document;
			this.filter = filter;
			this.lstxpath = lstXpaths;
			this.included = included;
			this.runInUnrestrictedMode = runInUnrestrictedMode;
		}

		public Map<String,Property> getProperties(){
			return mapPpties;
		}
		
		@Override
		public void run() throws ClientException {
			DocumentModelList lstParent =  getParentList(session, this.doc, this.filter, this.runInUnrestrictedMode, true, this.included);
			DocumentModel parent = null;
			if (lstParent != null && !lstParent.isEmpty()) {
				parent = lstParent.get(0);
			}
			if(parent!=null){
			this.mapPpties = new HashMap<String, Property>(lstxpath.size());
			for (String xpath : this.lstxpath) {
				mapPpties.put(xpath, parent.getProperty(xpath));
			}
			}
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
	 * 
	 * @param session
	 * @param doc
	 * @param aclName par défaut Local
	 * @param filter
	 * @return
	 * @throws ClientException 
	 */
	public static ACL getDocumentACL(CoreSession session, DocumentModel doc,String aclName, ToutaticeFilter<ACE> filter) throws ClientException{
		ACL res = new ACLImpl();
			
		if (StringUtils.isBlank(aclName)) {
			aclName=ACL.LOCAL_ACL;
		}
		    			
		ACP acp = doc.getACP();
		ACL[] aclList = null;
		if ("*".equals(aclName)) {
			aclList = acp.getACLs();
		} else {
			ACL acl = acp.getACL(aclName);
			if (null != acl) {
				aclList = new ACLImpl[1];
				aclList[0] = acl;
			}
		}
		
		if (null != aclList) {
			for (ACL acl : aclList) {
				for (ACE ace : acl.getACEs()) {
					if (filter==null || filter.accept(ace)) {
						// ajouter les permissions au document doc
						res.add(ace);
					}
				}		       
			}
		}
		
		return res;
	}
	
	/**
	 * ajout une ace sur un document
     * 
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
	
	/**
	 * Méthode permettant d'appeler une opération Nuxeo..
	 * 
	 * @param automation
	 *            Service automation
	 * @param ctx
	 *            Contexte d'exécution
	 * @param operationId
	 *            identifiant de l'opération
	 * @param parameters
	 *            paramètres de l'opération
	 * @return le résultat de l'opération dont le type n'est pas connu à
	 *         priori
	 * @throws ServeurException
	 * @deprecated use {@link #ToutaticeOperationHelper.callOperation()} instead. 
	 */
	@Deprecated
    public static Object callOperation(AutomationService automation, OperationContext ctx, String operationId, Map<String, Object> parameters) throws Exception {
		InvokableMethod operationMethod = getRunMethod(automation, operationId);
		Object operationRes = operationMethod.invoke(ctx, parameters);
		return operationRes;
	}

	/**
	 * Check whether the document is a runtime (technical) document.
	 * 
	 * @param document the document to check
	 * @return  true if the document is runtime type, otherwise false.
	 */
	public static boolean isRuntimeDocument(DocumentModel document) {
		return document.hasFacet(FacetNames.SYSTEM_DOCUMENT) || document.hasFacet(FacetNames.HIDDEN_IN_NAVIGATION);
	}

	/**
	 * Méthode permettant de récupérer la méthode d'exécution (run()) d'une
	 * opération.
	 * 
	 * @param automation
	 *            instance du service d'automation
	 * @param operationId
	 *            identifiant de l'opération
	 * @return la méthode run() de l'opération
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws OperationNotFoundException
	 */
	private static InvokableMethod getRunMethod(AutomationService automation, String operationId)
			throws SecurityException, NoSuchMethodException, OperationNotFoundException {
		OperationType opType = automation.getOperation(operationId);
		Method method;
		try{
			method= opType.getType().getMethod("run", (Class<?>[]) null);
		}catch(NoSuchMethodException nsme){
			Class[] tabArg = new Class[1];
			tabArg[0] = DocumentModel.class;
			method= opType.getType().getMethod("run", tabArg);
		}
		OperationMethod anno = method.getAnnotation(OperationMethod.class);

		return new InvokableMethod(opType, method, anno);
	}
	
    public static DocumentModel getMediaSpace(DocumentModel doc,CoreSession session) throws ClientException {
    	DocumentModel mediaSpace=null;
    	DocumentModel currentDomain = ToutaticeDocumentHelper.getDomain(session, doc, true);
        if(currentDomain!=null){
		    String searchMediaLibraries = "ecm:primaryType = '" + MEDIALIB + "' and ecm:path startswith '" + currentDomain.getPathAsString()
		            + "' and ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'";
		
		    String queryMediaLibraries = String.format("SELECT * FROM Document WHERE %s", searchMediaLibraries);
		
		    DocumentModelList query = session.query(queryMediaLibraries);
		    
		    if (query.size() < 1 || query.size() > 1) {
		        mediaSpace = null;
		    } else
		        mediaSpace = query.get(0);
        }else{
        	log.warn("CurrentDomain not available "+doc.getTitle());
        }

        return mediaSpace;
    }
    
    /**
     * @param document
     * @return list of remote proxies of document (if any).
     */
     public static DocumentModelList getRemotePublishedDocuments(CoreSession session, DocumentModel document) {
         DocumentModelList remoteProxies = new DocumentModelListImpl();
         
         if(!ToutaticeDocumentHelper.isInPublishSpace(session, document)){
             DocumentModelList remoteProxiesFound = ToutaticeDocumentHelper.getProxies(session, document, ToutaticeGlobalConst.CST_TOUTATICE_PROXY_LOOKUP_SCOPE.GLOBAL, StringUtils.EMPTY, false);
             if(CollectionUtils.isNotEmpty(remoteProxiesFound)){
                 remoteProxies.addAll(remoteProxiesFound);
             }
         }
         
        return remoteProxies;

    }
     
     /**
      * @param document
      * @return true if document still exists.
      * @throws ClientException 
      */
     public static boolean isDocStillExists(CoreSession session,
             DocumentModel document) throws ClientException {
         boolean exists = false;

         if (document != null) {
             try {

                 session.getDocument(document.getRef());
                 exists = true;

             } catch (ClientException ce) {
                 
                 if (ce.getCause() instanceof NoSuchDocumentException) {
                     exists = false;
                 } else {
                     throw ce;
                 }
                 
             }
         }

         return exists;
     }
     
    /**
     * 
     * @param document
     * @return true if document is a remote proxy.
     */
    public static boolean isRemoteProxy(DocumentModel document) {
        return document.isProxy() && !StringUtils.endsWith(document.getName(), ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
    }
    
    /**
     * @param document
     * @return true if document is local proxy.
     */
    public static boolean isLocaProxy(DocumentModel document){
        return document.isProxy() && StringUtils.endsWith(document.getName(), ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
    }
    
    /**
     * @param session
     * @param document
     * @return true if working copy of document is different from last version.
     */
    public static boolean isBeingModified(CoreSession session, DocumentModel document) {
        boolean is = false;
        
        String versionLabel = document.getVersionLabel();
        
        DocumentModel lastDocumentVersion = session.getLastDocumentVersion(document.getRef());
        if(lastDocumentVersion != null){
            String lastDocumentVersionLabel = lastDocumentVersion.getVersionLabel();
            is = !StringUtils.equals(versionLabel, lastDocumentVersionLabel);
        }
        
        return is;
    }

}
