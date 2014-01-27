package fr.toutatice.ecm.platform.web.document;

import static org.jboss.seam.ScopeType.CONVERSATION;
import static org.jboss.seam.ScopeType.EVENT;
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PagedDocumentsProvider;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.picture.web.PictureBookManager;
import org.nuxeo.ecm.platform.types.SubType;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.model.SelectDataModel;
import org.nuxeo.ecm.platform.ui.web.model.impl.SelectDataModelImpl;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActionsBean;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.ecm.webapp.pagination.ResultsProvidersCache;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.GlobalConst;
import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeOperationHelper;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;

/**
 * @author oadam
 * 
 */
@Name("documentActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeDocumentActionsBean extends DocumentActionsBean implements ToutaticeDocumentActions, Serializable {

	private static final long serialVersionUID = -2085111938280655851L;

	private static final Log log = LogFactory.getLog(ToutaticeDocumentActionsBean.class);

	@In(create = true)
	protected transient NavigationContext navigationContext;

	@In(create = true)
	protected transient PictureBookManager pictureBookManager;

	@RequestParameter("type")
	protected String typeName;

	@RequestParameter("params")
	protected String reqParams;

	String newSwitchValue;
	String newKeyword;
	String source;
	Map<String, Boolean> mapSwitchState = null;

	static private final String CST_DEFAULT_PUBLICATON_AREA_TITLE = "inconnu";
	static private final String CST_DEFAULT_PUBLICATON_AREA_PATH = "/";
	static private final String CST_DEFAULT_UNKNOWN_VERSION_LABEL = "Version indéterminée"; // I18N

	@Create
	public void initialize() throws Exception {
		log.debug("Initializing...");
	}

	@Destroy
	@Remove
	@PermitAll
	public void destroy() {
		log.debug("Removing SEAM action listener...");
	}

	@PrePassivate
	public void saveState() {
		log.debug("PrePassivate");
	}

	@PostActivate
	public void readState() {
		log.debug("PostActivate");
	}

	public boolean getEveryThingRight() throws ClientException {
		return documentManager.hasPermission(navigationContext.getCurrentDocument().getRef(), SecurityConstants.EVERYTHING);
	}

	public boolean isRemoteProxy() {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		return currentDocument.isProxy() && !StringUtils.endsWith(currentDocument.getName(), GlobalConst.CST_PROXY_NAME_SUFFIX);
	}

	public String saveDocument() throws ClientException {
		DocumentModel changeableDocument = navigationContext.getChangeableDocument();
		updateDocWithMapSwitch(changeableDocument);

		return super.saveDocument(changeableDocument);
	}

	public String saveDocument(String viewId) throws ClientException {
		saveDocument();
		return viewId;
	}

	public String createMajorDocument() throws ClientException {
		String viewId = saveDocument();
		updateNUpgradeCurrentDocument("MAJOR");
		return viewId;
	}

	public String createMajorDocument(String viewId) throws ClientException {
		createMajorDocument();
		return viewId;
	}

	public String saveNSetOnLineDocument() throws ClientException {
		// sauvegarde
		DocumentModel changeableDocument = navigationContext.getChangeableDocument();
		updateDocWithMapSwitch(changeableDocument);
		String view = super.saveDocument(changeableDocument);

		// creation de la version MAJOR
		updateNUpgradeCurrentDocument("MAJOR");

		// mise en ligne
		DocumentModel newDocument = navigationContext.getCurrentDocument();
		setDocumentOnline(newDocument);

		return view;
	}

	public String saveNSetOnLineDocument(String viewId) throws ClientException {
		saveNSetOnLineDocument();
		return viewId;
	}

	public String createNSetOnLinePictureBook() throws Exception {
		String view = "";

		if (null != this.pictureBookManager) {
			// create
			view = this.pictureBookManager.createPictureBook();

			// mise en ligne
			DocumentModel newDocument = navigationContext.getCurrentDocument();
			setDocumentOnline(newDocument);
		} else {
			log.error("Failed to get the picture book manager from seam context");
		}

		return view;
	}

	public String createPictureBook(String viewId) throws Exception {
		if (null != this.pictureBookManager) {
			// create
			this.pictureBookManager.createPictureBook();
		} else {
			log.error("Failed to get the picture book manager from seam context");
		}

		return viewId;
	}

	public String createNSetOnLinePictureBook(String viewId) throws Exception {
		if (null != this.pictureBookManager) {
			// create
			this.pictureBookManager.createPictureBook();

			// mise en ligne
			DocumentModel newDocument = navigationContext.getCurrentDocument();
			setDocumentOnline(newDocument);
		} else {
			log.error("Failed to get the picture book manager from seam context");
		}

		return viewId;
	}

	/**
	 * surcharge de la méthode updateCurrentDocument de DocumentActionsBean pour
	 * faire la mise à jour en fonction de mapSwitchState
	 */
	public String updateCurrentDocument() throws ClientException {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		updateDocWithMapSwitch(currentDocument);
		String viewId = super.updateDocument(currentDocument);
		return viewId;
	}

	public String updateCurrentDocument(String viewId) throws ClientException {
		updateCurrentDocument();
		return viewId;
	}

	/**
	 * mise à jour et incrémentation de la version(MINOR ou MAJOR) du document courant
	 * 
	 * @param version MINOR ou MAJOR
	 * @return l'identifiant de la vue retour
	 * @throws ClientException
	 */
	public String updateNUpgradeCurrentDocument(String version) throws ClientException {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		VersioningOption vo = null;
		if ("MAJOR".equalsIgnoreCase(version)) {
			vo = VersioningOption.MAJOR;
		} else if ("MINOR".equalsIgnoreCase(version)) {
			vo = VersioningOption.MINOR;
		}
		currentDocument.putContextData(VersioningService.VERSIONING_OPTION, vo);
		return updateCurrentDocument();
	}

	/**
	 * mise à jour et incrémentation de la version(MINOR ou MAJOR) du document courant
	 * 
	 * @param version MINOR ou MAJOR
	 * @param viewId vue de redirection
	 * @return l'identifiant de la vue retour
	 * @throws ClientException
	 */
	public String updateNUpgradeCurrentDocument(String version, String viewId) throws ClientException {
		updateNUpgradeCurrentDocument(version);
		return viewId;
	}

	public String updateNSetOnLineCurrentDocument() throws ClientException {
		String view = null;

		// mise à jour
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		updateDocWithMapSwitch(currentDocument);
		view = super.updateDocument(currentDocument);

		// mise en ligne chaînée
		currentDocument = navigationContext.getCurrentDocument();
		setDocumentOnline(currentDocument);

		return view;
	}

	public String updateNSetOnLineCurrentDocument(String viewId) throws ClientException {
		updateNSetOnLineCurrentDocument();
		return viewId;
	}

	/**
	 * méthode permettant de prendre en compte les éléments de map
	 * mapSwitchState
	 * 
	 * @param document
	 *            document à mettre à jour
	 * @throws PropertyException
	 * @throws ClientException
	 */
	private void updateDocWithMapSwitch(DocumentModel document) throws PropertyException, ClientException {
		if (null == mapSwitchState) {
			return;
		}

		for (String key : mapSwitchState.keySet()) {

			if (mapSwitchState.get(key)) {
				document.setPropertyValue(key, null);
			}
		}
	}

	public String getNewKeyword() {
		return newKeyword;
	}

	public void setNewKeyword(String newKeyword) {
		this.newKeyword = newKeyword;
	}

	public String getNewSwitchValue() {
		return newSwitchValue;
	}

	public void setNewSwitchValue(String newSwitchValue) {
		this.newSwitchValue = newSwitchValue;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	private String extractExtension(String fileName) throws ClientException {
		String[] parts = fileName.split("\\.");
		if (parts.length > 1) {
			return parts[parts.length - 1];
		}
		return "none";
	}

	public boolean isFileMp3Playable(String fileName) throws ClientException {
		return extractExtension(fileName).equalsIgnoreCase("mp3");
	}

	public boolean isFileImage(String fileName) throws ClientException {
		return extractExtension(fileName).equalsIgnoreCase("gif") || extractExtension(fileName).equalsIgnoreCase("jpeg")
				|| extractExtension(fileName).equalsIgnoreCase("jpg") || extractExtension(fileName).equalsIgnoreCase("png");
	}

	@Factory(value = "currentEditedDocument", scope = EVENT)
	public DocumentModel factoryCurrentEditedDocument() {
		return navigationContext.getCurrentDocument();
	}

	/**
	 * Return the current document being either edited or created
	 * 
	 * @return the current document
	 * @throws ClientException
	 */
	public DocumentModel getCurrentDocument() throws ClientException {
		DocumentModel currentDoc = navigationContext.getChangeableDocument();

		if (!isNewlyCreatedChangeableDocument(currentDoc)) {
			// then, edition mode
			currentDoc = navigationContext.getCurrentDocument();
			if (currentDoc == null) {
				log.debug("current document cannot be initialized");
			}
		}

		return currentDoc;
	}

	private boolean isNewlyCreatedChangeableDocument(DocumentModel changeableDocument) throws ClientException {
		return (changeableDocument != null && changeableDocument.getId() == null && changeableDocument.getPath() == null && changeableDocument.getTitle() == null);
	}

	public boolean isOnlineDocument() throws ClientException {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		return isOnlineDocument(currentDocument);
	}

	/**
	 * Indique si le document courant possède une version en ligne
	 * 
	 * @param document
	 * @return true si le document courant possède une version en ligne. false
	 *         sinon.
	 * @throws ClientException
	 */
	public boolean isOnlineDocument(DocumentModel document) throws ClientException {
		return hasProxy(document);
	}

	/**
	 * @return true if the current document owns a proxy (is online)
	 * @throws ClientException
	 */
	public boolean hasProxy(DocumentModel document) throws ClientException {
		return (null != getProxy(document));
	}

	public boolean isOnlineWithSameVersion() {
		DocumentModel currentDocument = navigationContext.getCurrentDocument();
		return isOnlineWithSameVersion(currentDocument);
	}

	public String getProxyVersion(DocumentModel document) throws ClientException {
		String proxyVersion = ToutaticeDocumentHelper.getProxyVersion(documentManager, document);
		return (StringUtils.isNotBlank(proxyVersion) ? proxyVersion : CST_DEFAULT_UNKNOWN_VERSION_LABEL);
	}

	public DocumentModel getProxy(DocumentModel document) throws ClientException {
		return ToutaticeDocumentHelper.getProxy(documentManager, document, SecurityConstants.READ);
	}

	/**
	 * Determine si l'action "seeOnlineDocumentVersion" doit être présentée.
	 * 
	 * <h4>Conditions</h4> <li>le document doit posséder un proxy local (publication local pour mise en ligne)</li> <li>le document ne doit pas être la version
	 * valide</li>
	 * 
	 * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isSeeOnlineDocumentVersionActionAuthorized() {
		boolean status = false;
		DocumentModel currentDoc = null;

		// vérifie si un proxy existe
		try {
			currentDoc = navigationContext.getCurrentDocument();
			String proxyVersionLabel = getProxyVersion(currentDoc);
			if (!CST_DEFAULT_UNKNOWN_VERSION_LABEL.equals(proxyVersionLabel)) {
				status = !currentDoc.getVersionLabel().equals(proxyVersionLabel);
			}
		} catch (Exception e) {
			String docName = (null != currentDoc) ? currentDoc.getName() : "unknown";
			log.debug("Failed to check the online status of the document '" + docName + "', error: " + e.getMessage());
		}

		return status;
	}

	/**
	 * Determine si l'action "seeLatestValidDocumentVersion" doit être présentée.
	 * 
	 * <h4>Conditions</h4> <li>le document doit posséder une version valide</li> <li>le document ne doit pas être la version valide</li>
	 * 
	 * @return true si l'action doit être présentée. false sinon.
	 */
	public boolean isSeeLatestValidDocumentVersionActionAuthorized() {
		boolean status = false;
		DocumentModel currentDoc = null;

		// Récupérer la dernière version validée
		try {
			currentDoc = navigationContext.getCurrentDocument();
			DocumentModel validDocument = ToutaticeDocumentHelper.getLatestDocumentVersion(currentDoc, documentManager);
			if (null != validDocument) {
				status = !currentDoc.getVersionLabel().equals(validDocument.getVersionLabel());
			}
		} catch (Exception e) {
			String docName = (null != currentDoc) ? currentDoc.getName() : "unknown";
			log.debug("Failed to get the latest valid version of document '" + docName + "', error: " + e.getMessage());
		}

		return status;
	}

	/**
	 * Determine si l'action "seeLiveDocumentVersion" (fichier
	 * 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
	 * 
	 * <h4>Conditions</h4> 
	 * <li>le document visualisé ne doit pas être la version live</li> 
	 * <li>l'usager connecté doit avoir au minima un droit de lecture sur le doucment source</li>
	 * 
	 * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isSeeLiveDocumentVersionActionAuthorized() {
		boolean status = false;
		DocumentModel currentDoc = null;

		try {
			currentDoc = navigationContext.getCurrentDocument();
			DocumentModel sourceVersionDoc = documentManager.getSourceDocument(currentDoc.getRef());
			/* FIXME: api au lieu de test "empirique? */
			boolean isDeleted = sourceVersionDoc.getPath() == null;
			if (!isDeleted && !sourceVersionDoc.getId().equals(currentDoc.getId())) {
				if (documentManager.hasPermission(sourceVersionDoc.getRef(), SecurityConstants.READ)) {
					status = true;
				}
			}
		} catch (Exception e) {
			String docName = (null != currentDoc) ? currentDoc.getName() : "unknown";
			log.debug("Failed to check the status to see the live version of the document '" + docName + "', error: " + e.getMessage());
		}

		return status;
	}

	public String viewLiveVersion() throws ClientException {
		String output = "";

		DocumentModel currentDoc = navigationContext.getCurrentDocument();
		try {
			DocumentModel sourceVersionDoc = documentManager.getSourceDocument(currentDoc.getRef());
			if (null != sourceVersionDoc) {
				DocumentModel liveDoc = documentManager.getSourceDocument(sourceVersionDoc.getRef());
				if (null != liveDoc) {
					output = navigationContext.navigateToDocument(liveDoc);
				}
			}
		} catch (ClientException e) {
			log.info("The proxy document (' " + currentDoc.getName() + "') has lost its reference to the version document");
			facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor.getMessages().get("label.acaren.viewlive.reference.lost"));
		}

		return output;
	}

	public String viewArchivedVersion() throws ClientException {
		return viewArchivedVersion(navigationContext.getCurrentDocument());
	}

	public String viewArchivedVersion(DocumentModel document) throws ClientException {
		String output = "";

		try {
			DocumentModel archivedDocument = ToutaticeDocumentHelper.getLatestDocumentVersion(document, documentManager);
			if (null != archivedDocument) {
				output = navigationContext.navigateToDocument(document, ToutaticeDocumentHelper.getVersionModel(archivedDocument));
			}
		} catch (DocumentException e) {
			throw new ClientException(e);
		}

		return output;
	}

	public String viewOnlineVersion() throws ClientException {
		return viewOnlineVersion(navigationContext.getCurrentDocument());
	}

	public String viewOnlineVersion(DocumentModel document) throws ClientException {
		String output = "";

		try {
			DocumentModel proxy = getProxy(document);
			if (null != proxy) {
				String srcDocId = proxy.getSourceId();
				DocumentModel srcDoc = documentManager.getDocument(new IdRef(srcDocId));
				output = navigationContext.navigateToDocument(document, ToutaticeDocumentHelper.getVersionModel(srcDoc));
			}
		} catch (DocumentException e) {
			throw new ClientException(e);
		}

		return output;
	}

	private void setDocumentOnline(DocumentModel document) {
		try {
			String proxyVersionLabel = getProxyVersion(document);
			if (!document.getVersionLabel().equals(proxyVersionLabel)) {
				if (documentManager.hasPermission(document.getRef(), NuxeoStudioConst.CST_PERM_VALIDATE)) {
					ToutaticeOperationHelper.runOperationChain(documentManager, NuxeoStudioConst.CST_OPERATION_DOCUMENT_PUBLISH_ONLY, document);
				} else {
					ToutaticeOperationHelper.runOperationChain(documentManager, NuxeoStudioConst.CST_OPERATION_DOCUMENT_PUBLISH_REQUEST, document);
				}
			}
		} catch (Exception e) {
			log.error("Failed to set online the document: '" + document.getName() + "', error: " + e.getMessage());
		}
	}

	/**
	 * Mettre en ligne une sélection de documents dans un content view (folder
	 * ou document non folderish)
	 */
	public void publishDocumentSelection() {
		List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

		if (currentDocumentSelection.isEmpty()) {
			return;
		}

		for (DocumentModel selectedDocument : currentDocumentSelection) {
			// publication d'un document
			setDocumentOnline(selectedDocument);
		}

		// Rafraîchir la liste de sélections
		documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);

		// Rafraîchir le content view
		DocumentModel currentFolder = navigationContext.getCurrentDocument();
		Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED, currentFolder);
	}

	/**
	 * Mettre hors ligne une sélection de documents dans un content view (folder
	 * ou document non folderish)
	 * 
	 * @throws ClientException
	 */
	public void unPublishDocumentSelection() throws ClientException {
		DocumentModel currentFolder = navigationContext.getCurrentDocument();
		List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

		if (currentDocumentSelection.isEmpty()) {
			return;
		}

		try {
			ToutaticeOperationHelper.runOperationChain(documentManager, NuxeoStudioConst.CST_OPERATION_DOCUMENT_UNPUBLISH_SELECTION, new DocumentModelListImpl(
					currentDocumentSelection));
		} catch (Exception e) {
			log.error("Failed to set offline the selection from the document: '" + currentFolder.getTitle() + "', error: " + e.getMessage());
		}

		// Rafraîchir la liste de sélections
		documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);

		// Rafraîchir le content view
		Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED, currentFolder);
	}

	@SuppressWarnings("static-access")
	public String formatMessages(String message, Object... params) throws ClientException {
		String formattedString = "";

		if (null != params && params.length > 0) {
			FacesMessage faceMsg = facesMessages.createFacesMessage(FacesMessage.SEVERITY_INFO, resourcesAccessor.getMessages().get(message), params);
			formattedString = faceMsg.getDetail();
		}

		return formattedString;
	}
	
	/**
	 * Détermine si le document courant appartient à un espace destiné à la
	 * publication dans le portail Toutatice </p>
	 * 
	 * @return 'true' si le document courant peut être publié dans le portail.
	 *         'false' sinon.
	 */
	public boolean belongToPublishSpace() {
		return (!GlobalConst.NULL_DOCUMENT_MODEL.getType().equals(((ToutaticeNavigationContext) navigationContext).getCurrentPublicationArea().getType()));
	}

	/**
	 * @return le nom de l'espace de publication dans le portail pour le
	 *         document courant
	 */
	public String getPublicationAreaNameOfCurrentDocument() {
		return getPublicationAreaName(navigationContext.getCurrentDocument());
	}

	/**
	 * Détermine si le document courant appartient à un espace de travail en
	 * ligne directe.
	 * 
	 * </p>
	 * 
	 * @return 'true' si le document appartient à une espace de travail. 'false'
	 *         sinon.
	 */
	public boolean belongToWorkSpace() {
		return (!GlobalConst.NULL_DOCUMENT_MODEL.getType().equals(((ToutaticeNavigationContext) navigationContext).getCurrentWorkspaceArea().getType()));
	}

	/**
	 * @return le nom de l'espace de publication dans le portail pour le
	 *         document courant
	 */
	public String getSpacePath() {
		return getSpacePath(navigationContext.getCurrentDocument());
	}

	/**
	 * @return le nom de l'espace de publication dans le portail pour le
	 *         document courant
	 */
	// TODO a déplacer dans le addon publication distante
	public List<String> getDocumentPathSegments(DocumentModel document, DocumentModel referenceDoc) {
		List<String> list = new ArrayList<String>();

		try {
			if ((null != referenceDoc && document.getId().equals(referenceDoc.getId())) || (NuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(document.getType()))) {
				return list;
			}

			list.add(document.getTitle());

			DocumentModel parent = documentManager.getParentDocument(document.getRef());
			list.addAll(getDocumentPathSegments(parent, referenceDoc));
		} catch (ClientException e) {
			log.error("Failed to get the path segments of document '" + document.getName() + "', error: " + e.getMessage());
		}

		return list;
	}

	/**
	 * @return le nom de l'espace de publication dans le portail pour la section
	 */
	// TODO a déplacer dans le addon publication distante
	public String getPublicationAreaNameOfSection(DocumentModel section) {
		String areaName = getPublicationAreaName(section);

		if (CST_DEFAULT_PUBLICATON_AREA_TITLE.equals(areaName)) {
			/*
			 * La section n'appartient pas à un espace de publication mais à un élément
			 * de type SectionRoot. Prendre son nom.
			 */
			DocumentModel sectionRoot = ((ToutaticeNavigationContext) navigationContext).getSectionPublicationArea(section);
			if (sectionRoot != null) {
				try {
					areaName = sectionRoot.getTitle();
				} catch (ClientException e) {
					log.error("Failed to get the domain title, error: " + e.getMessage());
					areaName = CST_DEFAULT_PUBLICATON_AREA_TITLE;
				}
			} else {
				/*
				 * La section n'appartient pas à un espace de publication ni à une SectionRoot. Prendre
				 * le nom du domaine à la place
				 */
				DocumentModel domain = ((ToutaticeNavigationContext) navigationContext).getDocumentDomain(section);
				if (domain != null) {
					try {
						areaName = domain.getTitle();
					} catch (ClientException e) {
						log.error("Failed to get the domain title, error: " + e.getMessage());
						areaName = CST_DEFAULT_PUBLICATON_AREA_TITLE;
					}
				}
			}
		}

		return areaName;
	}

	private String getSpacePath(DocumentModel document) {
		String path = CST_DEFAULT_PUBLICATON_AREA_PATH;
		DocumentModel space = ((ToutaticeNavigationContext) navigationContext).getPublicationArea(document);
		if (GlobalConst.NULL_DOCUMENT_MODEL.getType().equals(space.getType())) {
			space = navigationContext.getCurrentWorkspace();
		}
		if (null != space) {
			path = space.getPathAsString();
		}
		return path;
	}

	private String getPublicationAreaName(DocumentModel document) {
		String name = CST_DEFAULT_PUBLICATON_AREA_TITLE;

		DocumentModel area = ((ToutaticeNavigationContext) navigationContext).getPublicationArea(document);
		if (!GlobalConst.NULL_DOCUMENT_MODEL.getType().equals(area.getType())) {
			try {
				name = area.getTitle();
			} catch (ClientException e) {
				log.debug("Failed to get the publication area name, error: " + e.getMessage());
			}
		}

		return name;
	}

	private boolean isOnlineWithSameVersion(DocumentModel document) {
		boolean status = false;

		try {
			String onlineDocVersion = getProxyVersion(document);
			if (document.getVersionLabel().equals(onlineDocVersion)) {
				status = true;
			}
		} catch (Exception e) {
			log.debug("Failed to execute 'isOnlineWithSameVersion', error: " + e.getMessage());
		}

		return status;
	}

	/**
	 * Récupèrer le document de création (ChangeableDocument) ou le créer s'il
	 * n'existe pas.
	 * 
	 * @return le document nouvellement créé
	 */
	public DocumentModel getOrCreateChangeableDocument() throws ClientException, IOException, ClassNotFoundException {
		DocumentModel changeableDocument = navigationContext.getChangeableDocument();

		if (changeableDocument == null && typeName != null) {
			// vérifier que le document peut être créé dans le document courant
			// parent/containeur
			DocumentModel container = navigationContext.getCurrentDocument();
			Type cType = typeManager.getType(container.getType());
			Map<String, SubType> allowedSubTypes = cType.getAllowedSubTypes();

			if (allowedSubTypes.containsKey(typeName)) {
				// créer le changeable document du type réclamé
				createDocument(typeName);
				changeableDocument = navigationContext.getChangeableDocument();

			} else {
				return null;
			}

			if (reqParams != null && !"null".equalsIgnoreCase(reqParams)) {
				try {

					Base64 b64 = new Base64();
					byte[] tabByte = b64.decode(reqParams.getBytes());
					InputStream io = new ByteArrayInputStream(tabByte);

					ObjectInputStream ois = new ObjectInputStream(io);
					Object o = ois.readObject();
					Map<String, List<String>> params = new HashMap<String, List<String>>();
					if (o instanceof Map) {
						params = (Map<String, List<String>>) o;
					}
					for (String xpath : params.keySet()) {
						changeableDocument.setPropertyValue(xpath, (Serializable) params.get(xpath));
					}
				} catch (ClassNotFoundException cnfe) {
					log.error("*** ERROR " + cnfe.getMessage());
					new ClientException("Impossible de lire le(s) logiciel(s) sélectionné(s)");
				} catch (IOException ioe) {
					log.error("*** ERROR " + ioe.getMessage());
					new ClientException("Impossible de lire le(s) logiciel(s) sélectionné(s)");
					// FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible de lire le(s) logiciel(s) sélectionné(s)", new Object());
					// navigateToView("toutatice_error");
				}

			}
		}

		return changeableDocument;
	}

	@Factory(value = "currentPictureBookChildrenSelectModel", scope = ScopeType.EVENT)
	public SelectDataModel getPictureBookChildrenSelectModel() throws ClientException {
		DocumentModelList documents = getCurrentPictureBookChildrenPage();
		List<DocumentModel> selectedDocuments = documentsListsManager.getWorkingList(DocumentsListsManager.CURRENT_DOCUMENT_SELECTION);
		SelectDataModel model = new SelectDataModelImpl(NuxeoStudioConst.CHILDREN_DOCUMENT_LIST, documents, selectedDocuments);
		model.addSelectModelListener(this);
		return model;
	}

	private DocumentModelList getCurrentPictureBookChildrenPage() throws ClientException {
		final String logPrefix = "<getCurrentPictureBookChildrenPage> ";
		DocumentModelList currentDocumentChildren = new DocumentModelListImpl();

		if (documentManager == null) {
			log.error(logPrefix + "documentManager not initialized");
		} else {
			try {
				ResultsProvidersCache resultsProvidersCache = (ResultsProvidersCache) Component.getInstance("resultsProvidersCache");
				resultsProvidersCache.invalidate("ACAREN_CURRENT_PICTUREBOOK_CHILDREN");
				PagedDocumentsProvider resultsProvider = resultsProvidersCache.get("ACAREN_CURRENT_PICTUREBOOK_CHILDREN");
				currentDocumentChildren = resultsProvider.getCurrentPage();
			} catch (Throwable t) {
				log.error("Failed to get the list of images within picture book, error: " + t.getMessage());
			}
		}

		return currentDocumentChildren;
	}

	/**
	 * Define whether all the fields of the list have an empty value
	 * 
	 * @param doc
	 *            the current document in context
	 * @param fields
	 *            the fields to analyze
	 * @return true if all fields have an empty value. false otherwise
	 */
	public boolean areWidgetFieldsAllEmpty(DocumentModel doc, FieldDefinition[] fields) {
		boolean status = true;
		Serializable fieldValue;

		// check at least one field value is not empty
		for (FieldDefinition field : fields) {
			try {
				String xpath = field.getPropertyName();

				// handle the case where a specific indexed item is selected
				// within the field value (a list)
				if (xpath.matches(".*/\\d*")) {
					String[] xpathElts = xpath.split("/");
					xpath = xpathElts[0];
					int index = Integer.parseInt(xpathElts[1]);

					fieldValue = doc.getPropertyValue(xpath);
					String[] fieldValueList = (String[]) fieldValue;
					if (fieldValueList.length >= index) {
						fieldValue = (fieldValueList)[index];
					} else {
						fieldValue = null;
					}
				} else {
					fieldValue = doc.getPropertyValue(xpath);
				}

				// check the field value is not empty
				if (!isFieldValueEmpty(fieldValue)) {
					status = false;
					break;
				}
			} catch (Exception e) {
				try {
					log.debug("Failed to analyse the document fields (document name: '" + doc.getTitle() + "'), error:" + e.getMessage());
				} catch (ClientException e1) {
					// ignore
				}
			}
		}

		return status;
	}

	/**
	 * Check whether one field has an empty value according to its type
	 * 
	 * @param fieldValue
	 *            the field value
	 * @return true if the value is empty (null or empty string). false
	 *         otherwise
	 */
	private boolean isFieldValueEmpty(Serializable fieldValue) {
		boolean status = true;

		if (null != fieldValue) {
			if (fieldValue instanceof String) {
				String fieldValueStg = (String) fieldValue;
				if (StringUtils.isNotBlank(fieldValueStg)) {
					status = false;
				}
			} else if (fieldValue instanceof String[]) {
				String[] fieldValuesList = (String[]) fieldValue;
				if (fieldValuesList.length > 0) {
					for (String item : fieldValuesList) {
						if (!isFieldValueEmpty(item)) {
							status = false;
							break;
						}
					}
				}
			} else {
				status = false;
			}
		}

		return status;
	}

	public void setIsSwitchState(boolean isSwitchState, String param) {
		this.mapSwitchState.put(param, isSwitchState);
	}

	@Observer(value = {EventNames.DOCUMENT_SELECTION_CHANGED})
	public void resetChgDocument() {
		navigationContext.setChangeableDocument(null);
	}

	@Observer(value = {EventNames.DOCUMENT_SELECTION_CHANGED, EventNames.NEW_DOCUMENT_CREATED})
	public Map<String, Boolean> razMapSwitchState() {
		if (null == mapSwitchState) {
			mapSwitchState = new HashMap<String, Boolean>();
		} else {
			mapSwitchState.clear();
		}
		return mapSwitchState;
	}

	@Observer(value = {EventNames.NEW_DOCUMENT_CREATED})
	public void initShowInMenu() throws ClientException {
		DocumentModel newDocument = navigationContext.getChangeableDocument();

		if (newDocument.hasFacet(NuxeoStudioConst.CST_FACET_SPACE_NAVIGATION_ITEM) 
				|| "Folder".equals(newDocument.getType())
				|| "OrderedFolder".equals(newDocument.getType())) {
			newDocument.setPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_SIM, true);
			if ("PortalSite".equals(newDocument.getType())) {
				newDocument.setPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_INTERNAL_CONTEXTUALIZATION, true);
			}
		}
	}


	public boolean showShowInMenu() throws ClientException {
		boolean res = false;
		DocumentModel newDocument = navigationContext.getChangeableDocument();
		if (newDocument == null) {
			newDocument = navigationContext.getCurrentDocument();
		}

		if (newDocument.hasFacet(NuxeoStudioConst.CST_FACET_SPACE_CONTENT) || "Folder".equals(newDocument.getType())
				|| "OrderedFolder".equals(newDocument.getType())) {
			res = true;
		}

		return res;
	}

	public boolean initSwitchState(String param) throws PropertyException, ClientException {
		if (null == mapSwitchState) {
			mapSwitchState = new HashMap<String, Boolean>();
		}

		if (mapSwitchState.get(param) == null) {

			DocumentModel currentDoc = getCurrentDocument();
			if (currentDoc != null && param != null) {
				Object value = currentDoc.getPropertyValue(param);
				if (value == null || (value instanceof String[] && ((String[]) value).length == 0)) {
					mapSwitchState.put(param, true);
				} else {
					mapSwitchState.put(param, false);
				}
			}
		}

		return mapSwitchState.get(param);
	}

	public String navigateToView(String viewId) throws ClientException {

		DocumentModel doc = navigationContext.getCurrentDocument();
		return navigationContext.navigateToDocument(doc, viewId);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public String addDocumentKeyword() throws ClientException {
		if (newKeyword == null || "".equals(newKeyword)) {
			return null;
		}

		// Récupération des la liste des mots-clefs existants
		DocumentModel currentDoc = getCurrentDocument();
		if (currentDoc != null) {
			Object subjectsObj = currentDoc.getProperty("toutatice", "keywords");
			List<String> subjects;
			if (subjectsObj != null) {
				if (subjectsObj instanceof List) {
					subjects = (List) subjectsObj;
				} else {
					String[] subjectsArray = (String[]) subjectsObj;
					subjects = Arrays.asList(subjectsArray);
					subjects = new ArrayList<String>(subjects);
				}
			} else {
				subjects = new ArrayList<String>();
			}

			// Nettoyage des espaces superflus
			if (newKeyword.contains("  ")) {
				newKeyword = newKeyword.replaceAll("  ", " ");
			}
			// Nettoyage des espaces superflus
			if (newKeyword.contains(", ") || newKeyword.contains(" ,")) {
				newKeyword = newKeyword.replaceAll(", ", ",");
				newKeyword = newKeyword.replaceAll(" ,", ",");
			}
			// Ségmentation en mots-clefs
			String[] s = newKeyword.split(",");

			// Itération sur la liste des mots-clefs
			for (int i = 0; i < s.length; i++) {
				subjects.add(s[i]);
			}

			newKeyword = "";
			// Affectation de la nouvelle liste à la métadonnée "subjects"
			currentDoc.setProperty("toutatice", "keywords", subjects);
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public String removeDocumentKeyword() throws ClientException {
		DocumentModel currentDoc = getCurrentDocument();
		if (currentDoc != null) {
			Object subjectsObj = currentDoc.getProperty("toutatice", "keywords");
			List<String> subjects;
			if (subjectsObj instanceof List) {
				subjects = (List) subjectsObj;
			} else {
				String[] subjectsArray = (String[]) subjectsObj;
				subjects = Arrays.asList(subjectsArray);
				subjects = new ArrayList<String>(subjects);
			}
			FacesContext context = FacesContext.getCurrentInstance();
			String subject = context.getExternalContext().getRequestParameterMap().get("subject");
			subjects.remove(subject);
			currentDoc.setProperty("toutatice", "keywords", subjects);
		}
		return null;
	}

}