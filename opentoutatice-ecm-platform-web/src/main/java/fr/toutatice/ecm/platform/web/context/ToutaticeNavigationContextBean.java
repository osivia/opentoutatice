package fr.toutatice.ecm.platform.web.context;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.ui.web.pathelements.ArchivedVersionsPathElement;
import org.nuxeo.ecm.platform.ui.web.pathelements.DocumentPathElement;
import org.nuxeo.ecm.platform.ui.web.pathelements.PathElement;
import org.nuxeo.ecm.platform.ui.web.pathelements.VersionDocumentPathElement;
import org.nuxeo.ecm.webapp.context.NavigationContextBean;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.GlobalConst;
import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

@Name("navigationContext")
@Scope(CONVERSATION)
@Install(precedence = Install.DEPLOYMENT)
public class ToutaticeNavigationContextBean extends NavigationContextBean implements ToutaticeNavigationContext {

	/**
	 * Olivier Adam, Rectorat de Rennes
	 * Ce bean permet de récupérer le domaine de métadonnée du domaine ouvert dans le contexte de navigation
	 */
	private static final long serialVersionUID = -2641552800368018402L;

	private static final Log log = LogFactory.getLog(ToutaticeNavigationContextBean.class);

	// variables pour gérer le cache
	private Map<String, DocumentModelList> currentDocumentParentsListMap = null;
	private Map<String, DocumentModel> spaceDocMap = null;
	private Map<String, DocumentModel> sectionPublicationAreaMap = null;
	private Map<String, DocumentModel> documentDomainMap = null;
	private List<PathElement> parents;

	public String getCurrentLifeCycleState() throws ClientException {
		try {
			DocumentModel currDoc = getCurrentDocument();
			return documentManager.getCurrentLifeCycleState(currDoc.getRef());
		} catch (ClientException e) {
			throw new ClientRuntimeException(e);
		}
	}

	public DocumentModel getDocumentDomain(DocumentModel document) {
		if (null == documentDomainMap) {
			documentDomainMap = new HashMap<String, DocumentModel>();
		}

		if (null == documentDomainMap.get(document.getId())) {
			try {
				UnrestrictedSessionRunner runner = new UnrestrictedGetDocumentDomainRunner(documentManager, document);
				runner.runUnrestricted();
			} catch (Exception e) {
				log.error("Failed to retrieve the document domain for '" + document.getName() + "', error: " + e.getMessage());
			}
		}

		return documentDomainMap.get(document.getId());
	}

	private class UnrestrictedGetDocumentDomainRunner extends UnrestrictedSessionRunner {

		private DocumentModel baseDoc;

		public UnrestrictedGetDocumentDomainRunner(CoreSession session, DocumentModel document) {
			super(session);
			this.baseDoc = document;
		}

		@Override
		public void run() throws ClientException {
			DocumentModelList currentParentsList = getCurrentDocumentParentsList(this.session, this.baseDoc);
			if ((null != currentParentsList) && (0 < currentParentsList.size())) {
				for (DocumentModel parent : currentParentsList) {
					if (NuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(parent.getType())) {
						documentDomainMap.put(this.baseDoc.getId(), parent);
						break;
					}
				}
			}
		}

	}

	public DocumentModel getCurrentPublicationArea() {
		return getPublicationArea(getCurrentDocument());
	}

	public DocumentModel getPublicationArea(DocumentModel document) {
		DocumentModel area = GlobalConst.NULL_DOCUMENT_MODEL;
		
		if (null != document) {
			area = getSpaceDoc(document);
			if (!ToutaticeDocumentHelper.isAPublicationSpaceDocument(area)) {
				area = GlobalConst.NULL_DOCUMENT_MODEL;
			}
		} else {
			log.warn("Failed to get the publication area: null current document.");
		}
		
		return area;
	}
	
	public DocumentModel getCurrentWorkspaceArea() {
		return getWorkspaceArea(getCurrentDocument());
	}

	public DocumentModel getWorkspaceArea(DocumentModel document) {
		DocumentModel area = GlobalConst.NULL_DOCUMENT_MODEL;
		
		if (null != document) {
			DocumentModel space = getSpaceDoc(document);
			if (ToutaticeDocumentHelper.isAWorkSpaceDocument(space)) {
				area = space;
			}
		} else {
			log.warn("Failed to get the workspace area: null current document.");
		}
		
		return area;
	}

	public DocumentModel getCurrentSpaceDoc() {
		return getSpaceDoc(getCurrentDocument());
	}
	
	public DocumentModel getSpaceDoc(DocumentModel document) {
		if (null == document) {
			log.warn("Failed to get the space doc: null current document.");
			return GlobalConst.NULL_DOCUMENT_MODEL;
		}

		if (null == spaceDocMap) {
			spaceDocMap = new HashMap<String, DocumentModel>();
		}

		if (null == spaceDocMap.get(document.getId())) {
			// initialiser le cache avec: recherche déjà effectuée mais pas de résultat
			spaceDocMap.put(document.getId(), GlobalConst.NULL_DOCUMENT_MODEL);
			
			try {
				DocumentModelList spaceDocsList = ToutaticeDocumentHelper.getParentSpaceList(documentManager, document, true, true, true);
				if (null != spaceDocsList && !spaceDocsList.isEmpty()) {
					spaceDocMap.put(document.getId(), spaceDocsList.get(0));
				}
			} catch (Exception e) {
				log.error("Failed to determine the parent space document of the document '" + document.getName() + "', error: " + e.getMessage());
			}
		}

		return spaceDocMap.get(document.getId());
	}

	public DocumentModel getSectionPublicationArea(DocumentModel section) {
		return getSectionPublicationArea(section, true);
	}

	public DocumentModel getSectionPublicationArea(DocumentModel section, boolean getHead) {
		if (null == sectionPublicationAreaMap) {
			sectionPublicationAreaMap = new HashMap<String, DocumentModel>();
		}

		if (null == sectionPublicationAreaMap.get(section.getId())) {
			try {
				UnrestrictedSessionRunner runner = new UnrestrictedGetSectionPublicationAreaRunner(documentManager, section, getHead);
				runner.runUnrestricted();
			} catch (Exception e) {
				log.error("Failed to determine if the section '" + section.getName() + "' belongs to a publication area, error: " + e.getMessage());
			}
		}

		return sectionPublicationAreaMap.get(section.getId());
	}
	
	private class UnrestrictedGetSectionPublicationAreaRunner extends UnrestrictedSessionRunner {
		private DocumentModel baseDoc;
		private boolean getHead = false;

		protected UnrestrictedGetSectionPublicationAreaRunner(CoreSession session, DocumentModel section, boolean getHead) {
			super(session);
			this.baseDoc = section;
			this.getHead = getHead;
		}

		@Override
		public void run() throws ClientException {
			// initialiser le cache avec: recherche déjà effectuée mais pas de résultat
			sectionPublicationAreaMap.put(this.baseDoc.getId(), GlobalConst.NULL_DOCUMENT_MODEL);

			// rechercher
			DocumentModelList currentParentsList = getCurrentDocumentParentsList(this.session, this.baseDoc);
			if ((null != currentParentsList) && (0 < currentParentsList.size())) {
				for (DocumentModel parent : currentParentsList) {
					if (parent.hasFacet(FacetNames.MASTER_PUBLISH_SPACE)) {
						sectionPublicationAreaMap.put(this.baseDoc.getId(), parent);
						if (!this.getHead) {
							break;
						}
					}
				}
			}
		}

	}

	/**
	 * Retourne la liste ordonnée des parents du document courant en commençant par le document courant 
	 * lui-même. Si le document courant est un folder, le placer en premier élément de la liste car il
	 * peut avoir lui-même les méta-données scrutées.
	 * <br/>
	 * <br/>--- ! ATTENTION ! --- Cette méthode doit être appelée par une méthode en mode session unrestricted.
	 * 
	 * @return liste des parents.
	 */
	private DocumentModelList getCurrentDocumentParentsList(CoreSession session, DocumentModel baseDoc) {

		if (null == currentDocumentParentsListMap) {
			currentDocumentParentsListMap = new HashMap<String, DocumentModelList>();
		}

		if (null == currentDocumentParentsListMap.get(baseDoc.getId())) {
			try {
				DocumentRef[] parentsRefsList;
				DocumentModelList currentDocumentParentsList = new DocumentModelListImpl();

				if (baseDoc.isFolder()) {
					currentDocumentParentsList.add(baseDoc);
				}

				parentsRefsList = session.getParentDocumentRefs(baseDoc.getRef());
				if (null != parentsRefsList && parentsRefsList.length > 0) {
					currentDocumentParentsList.addAll(session.getDocuments(parentsRefsList));
				}

				if (!currentDocumentParentsList.isEmpty()) {
					currentDocumentParentsListMap.put(baseDoc.getId(), currentDocumentParentsList);
				}
			} catch (ClientException e) {
				log.error("Failed to get the parent list for the current document, error: " + e.getMessage());
			}
		}

		return currentDocumentParentsListMap.get(baseDoc.getId());
	}

	//Fait par OA
	@Override
	protected void resetCurrentPath() throws ClientException {
		final String logPrefix = "<toutaticeResetCurrentPath> ";

		parents = new ArrayList<PathElement>();

		if (null == documentManager) {
			log.error(logPrefix + "documentManager not initialized");
			return;
		}

		if (currentDocument != null) {
			if (currentDocument.isVersion()) {
				DocumentModel sourceDocument = documentManager
				.getSourceDocument(currentDocument.getRef());

				List<DocumentModel> parentList = documentManager
				.getParentDocuments(sourceDocument.getRef());
				for (DocumentModel docModel : parentList) {
					parents.add(getDocumentPathElement(docModel));
				}

				parents.add(new ArchivedVersionsPathElement(sourceDocument));
				parents.add(new VersionDocumentPathElement(currentDocument));
			} else if (currentDocumentParents != null) {
				for (DocumentModel docModel : currentDocumentParents) {
					parents.add(getDocumentPathElement(docModel));
				}
				//Spécificité de Toutatice : un document Live 
				if (currentDocument.isProxy() 
					&& NuxeoStudioConst.CST_DOC_STATE_APPROVED.equals(currentDocument.getCurrentLifeCycleState())) {
					UnrestrictedGetParentsInfoRunner runner = new UnrestrictedGetParentsInfoRunner(documentManager, currentDocument);
					runner.runUnrestricted();
				}
			}
		}
	}
	
	private class UnrestrictedGetParentsInfoRunner extends UnrestrictedSessionRunner {

		private DocumentModel baseDoc;
		
		public UnrestrictedGetParentsInfoRunner(CoreSession session, DocumentModel document) {
			super(session);
			this.baseDoc = document;
		}

		@Override
		public void run() throws ClientException {
			DocumentModel liveDoc = this.session.getWorkingCopy(this.baseDoc.getRef());
			int index = (parents.size() > 0) ? parents.size() - 1 : 0;
			
			//Traitement particulier si le proxy existe dans le même folder que le live.
			DocumentRef liveParentRef = liveDoc != null ? liveDoc.getParentRef():null;
			DocumentRef currentDocumentParentRef = this.baseDoc.getParentRef();
			
			/*
			 * On vérifie que
			 * 	-- les parents du document live et du proxy sont les mêmes. 
			 *  -- l'utilisateur à un droit d'ecriture sur le document live
			 *  
			 *  Dans ce cas, le breadcrump présente ..>parent>document de travail>version en ligne>document proxy
			 *  sinon ..>parent>version en ligne>document proxy
			 * 
			 */
			if (liveParentRef!=null 
				&& this.session.hasPermission(liveDoc.getRef(), "WRITE")
				&& currentDocumentParentRef!=null
				&& liveParentRef.equals(currentDocumentParentRef)) {
				parents.add(index, new DocumentPathElement(liveDoc));
				index++;
			}
			parents.add(index, new ToutaticeProxyDocumentPathElement(this.baseDoc));
		}

	}

	@Override
	public List<PathElement> getCurrentPathList() throws ClientException {
		if (parents == null) {
			resetCurrentPath();
		}
		return parents;
	}
	
	@Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED, 
			EventNames.DOMAIN_SELECTION_CHANGED,
			EventNames.CONTENT_ROOT_SELECTION_CHANGED,
			EventNames.DOCUMENT_CHANGED,
			EventNames.GO_HOME}, create = false)
			public void resetNavigation() throws ClientException {
		currentDocumentParentsListMap = null;
		sectionPublicationAreaMap = null;
		documentDomainMap = null;
		DocumentModel currentDoc = getCurrentDocument();
		currentDoc.reset();
		currentDoc.refresh();
	}
	
}
