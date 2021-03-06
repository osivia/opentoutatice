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
 * mberhaut1
 */
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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.ListDiff;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.picture.web.PictureBookManager;
import org.nuxeo.ecm.platform.types.SubType;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.ui.web.util.files.FileUtils;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActionsBean;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.PortalConstants;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeImageCollectionHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeOperationHelper;
import fr.toutatice.ecm.platform.core.utils.exception.ToutaticeException;
import fr.toutatice.ecm.platform.services.permalink.PermaLinkService;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;
import fr.toutatice.ecm.platform.web.fragments.PageBean;
import fr.toutatice.ecm.platform.web.workflows.ToutaticeDocumentRoutingActionsBean;

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

    private InputStream uploadedImage;

    private String uploadedImageName;

    @In(create = true)
    protected transient NavigationContext navigationContext;

    @In(create = true)
    protected PageBean pageBean;

    @In(create = true)
    protected transient PictureBookManager pictureBookManager;

    protected transient PermaLinkService permaLinkService;

    protected transient ToutaticeDocumentRoutingActionsBean routingActionBean;

    @RequestParameter("type")
    protected String typeName;

    @RequestParameter("params")
    protected String reqParams;

    /** Used by Portal Views (information send to Portal) */
    protected boolean live = true;

    String newSwitchValue;
    String newKeyword;
    String source;
    Map<String, Boolean> mapSwitchState = null;


    static protected final String CST_DEFAULT_PUBLICATON_AREA_TITLE = "inconnu";
    static protected final String CST_DEFAULT_PUBLICATON_AREA_PATH = "/";
    static protected final String CST_DEFAULT_UNKNOWN_VERSION_LABEL = "Version indéterminée"; // I18N

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

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

    protected ToutaticeDocumentRoutingActionsBean getDocumentRoutingActionBean() {
        return (ToutaticeDocumentRoutingActionsBean) SeamComponentCallHelper.getSeamComponentByName("routingActions");
    }

    public boolean getEveryThingRight() throws ClientException {
        return documentManager.hasPermission(navigationContext.getCurrentDocument().getRef(), SecurityConstants.EVERYTHING);
    }

    public boolean checkPermission(DocumentModel document, String permission) throws ClientException {
        return documentManager.hasPermission(document.getRef(), permission);
    }

    public DocumentModel getParent(DocumentModel document, boolean unrestricted){
        DocumentModel parent = null;

        DocumentModelList parentList = ToutaticeDocumentHelper.getParentList(this.documentManager, document, null, unrestricted, true);
        if(CollectionUtils.isNotEmpty(parentList)){
            parent = parentList.get(0);
        }

        return parent;
    }

    public boolean isRemoteProxy() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return currentDocument.isProxy() && !StringUtils.endsWith(currentDocument.getName(), ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
    }

    /**
     * @param currentDocument
     * @return true if current document is remote proxy.
     */
    public boolean isRemoteProxy(DocumentModel currentDocument) {
        return currentDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY);
    }

    @Override
    public String saveDocument() throws ClientException {
        DocumentModel changeableDocument = navigationContext.getChangeableDocument();
        updateDocWithMapSwitch(changeableDocument);

        return super.saveDocument(changeableDocument);
    }

    public String saveDocument(String viewId) throws ClientException {
        saveDocument();

        String msgSuccessKey;
        if(isInPublishSpace(getCurrentDocument())){
            msgSuccessKey = PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name();;
        } else {
            msgSuccessKey = PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE_IN_WS.name();
        }

        pageBean.setNotificationKey(msgSuccessKey);
        live = true;
        return viewId;
    }

    @Override
    public String updateDocument(DocumentModel doc) throws ClientException{
    	return super.updateDocument(doc);
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
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name());

        // mise en ligne
        DocumentModel newDocument = navigationContext.getCurrentDocument();
        setDocumentOnline(newDocument);

        return view;
    }

    public String saveNSetOnLineDocument(String viewId) throws ClientException {
        saveNSetOnLineDocument();
        return viewId;
    }

    public String constraintImage(Integer imageIndex, String resConstraint)
			throws ToutaticeException {

        if (StringUtils.isBlank(resConstraint) || (StringUtils.split(resConstraint, 'x').length < 2)) {
			throw new ClientException("resConstraint must be provided");
		}

		DocumentModel currentDocument = navigationContext.getCurrentDocument();

		OperationContext ctx = new OperationContext(documentManager);

        String imageXpath = "ttc:images/" + imageIndex;
        String[] widthHeigth = StringUtils.split(resConstraint, 'x');
        Integer width = new Integer(widthHeigth[0]);
        Integer height = new Integer(widthHeigth[1]);

		ctx.setInput(currentDocument);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("xpath_img_in", imageXpath);
		parameters.put("xpath_img_out", imageXpath);
        parameters.put("img_width", width);
        parameters.put("img_heidth", height);
		parameters.put("enlarge", true);

		ToutaticeOperationHelper.callOperation(ctx, "ImageResize.Operation",
				parameters);

        return updateCurrentDocument("osivia_edit_attachments");
	}

    public String deleteImage(Integer imageIndex) {

		DocumentModel currentDocument = navigationContext.getCurrentDocument();

        List<Map<String, Object>> files = (List<Map<String, Object>>) currentDocument.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
        Object file = CollectionUtils.get(files, imageIndex);
        ToutaticeImageCollectionHelper.instance().remove(files, file);
        currentDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES, (Serializable) files);

        return updateCurrentDocument("osivia_edit_attachments");
	}

    public String uploadImage() throws ClientException {
        return uploadImage(null);
    }

    public String uploadImage(Integer imageIndex) throws ClientException {
        if ((uploadedImage == null) || (uploadedImageName == null)) {
            return "";
        }
        final DocumentModel doc = navigationContext.getCurrentDocument();

        final Map<String, Object> props = new HashMap<String, Object>();
        uploadedImageName = FileUtils.getCleanFileName(uploadedImageName);
        props.put("filename", uploadedImageName);
        props.put("file", FileUtils.createSerializableBlob(uploadedImage, uploadedImageName, null));
        final ListDiff listDiff = new ListDiff();

        if (imageIndex != null) {
            final List<Map<String, Object>> filesList = (List<Map<String, Object>>) doc.getProperty("files", "files");
            final int lastIndex = filesList == null ? 0 : filesList.size();
            int fileIndex = imageIndex > lastIndex ? lastIndex : imageIndex;
            listDiff.modify(fileIndex, props);
        } else {
            listDiff.add(props);
        }
        doc.setProperty("files", "files", listDiff);

        return "osivia_edit_attachments";
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
            pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name());
            live = true;
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
    @Override
    public String updateCurrentDocument() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        updateDocWithMapSwitch(currentDocument);
        String viewId = super.updateDocument(currentDocument);
        return viewId;
    }

    public String updateCurrentDocument(String viewId) throws ClientException {
        updateCurrentDocument();
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_MODIFY.name());
        live = true;
        return viewId;
    }

    /**
     * mise à jour et incrémentation de la version(MINOR ou MAJOR) du document courant
     *
     * @param version MINOR ou MAJOR
     * @return l'identifiant de la vue retour
     * @throws ClientException
     */
    @Override
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
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_MODIFY.name());
        live = true;
        return viewId;
    }

    public String updateNSetOnLineCurrentDocument() throws ClientException {
        String view = null;

        // mise à jour
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        updateDocWithMapSwitch(currentDocument);
        view = super.updateDocument(currentDocument);
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_MODIFY.name());

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
    @Override
    public void updateDocWithMapSwitch(DocumentModel document) throws PropertyException, ClientException {
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
        return ((changeableDocument != null) && (changeableDocument.getId() == null) && (changeableDocument.getPath() == null) && (changeableDocument.getTitle() == null));
    }

    public boolean isOnlineDocument() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isOnlineDocument(currentDocument);
    }

    /**
     * @return true if given document is in Publish Space.
     */
    public boolean isInPublishSpace(DocumentModel document){
        return ToutaticeDocumentHelper.isInPublishSpace(documentManager, document);
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
    	boolean res = false;
    	try{
    	    if(ToutaticeDocumentHelper.isDocStillExists(documentManager, document)){
                res = hasProxy(document);
            }
    	}catch(DocumentSecurityException se){
    		// dans le cadre de la publication profilée
    		// re-visualisation d'un contentview contenant un document qui n'est plus visible par l'utilisateur courant
    		// le document live est encore dans le cache par contre son proxy est mis à jour est n'est plus visible par l'U
    		log.warn(se.getMessage());
    	}
        return res;
    }

    /**
     * @return true if the current document owns a proxy (is online)
     * @throws ClientException
     */
    @Override
    public boolean hasProxy(DocumentModel document) throws ClientException {
        return (null != getProxy(document));
    }

    public boolean isOnlineWithSameVersion() {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isOnlineWithSameVersion(currentDocument);
    }

    @Override
    public String getProxyVersion(DocumentModel document) throws ClientException {
        String proxyVersion = ToutaticeDocumentHelper.getProxyVersion(documentManager, document);
        return (StringUtils.isNotBlank(proxyVersion) ? proxyVersion : CST_DEFAULT_UNKNOWN_VERSION_LABEL);
    }

    @Override
    public DocumentModel getProxy(DocumentModel document) throws ClientException {
        return ToutaticeDocumentHelper.getProxy(documentManager, document, SecurityConstants.READ);
    }

    /**
     * Determine si l'action "seeOnlineDocumentVersion" doit être présentée.
     *
     * <b>Conditions</b> <ul>
     * <li>le document doit posséder un proxy local (publication locale pour mise en ligne)</li>
     * <li>le document ne doit pas être la version
     * valide</li>
     * </ul>
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
     * Determine si l'action "seeLiveDocumentVersion" de la vue 'summary' doit être présentée.
     *
     * <b>Conditions</b>
     * <ul><li>le document visualisé ne doit pas être la version live</li> <li>l'usager connecté doit avoir au minima un droit de lecture sur le
     * doucment source</li>
     * </ul>
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
            DocumentModel workingCopy = documentManager.getWorkingCopy(sourceVersionDoc.getRef());
            boolean isDeleted = (workingCopy == null)
                    || ((workingCopy != null) && LifeCycleConstants.DELETED_STATE.equals(workingCopy.getCurrentLifeCycleState()));
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
            facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor.getMessages().get("label.toutatice.viewlive.reference.lost"));
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

    protected void setDocumentOnline(DocumentModel document) {
        try {
            String proxyVersionLabel = getProxyVersion(document);
            if (!document.getVersionLabel().equals(proxyVersionLabel)) {
                if (documentManager.hasPermission(document.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE)) {
                    ToutaticeOperationHelper.runOperationChain(documentManager, ToutaticeNuxeoStudioConst.CST_OPERATION_DOCUMENT_PUBLISH_ONLY, document);
                    pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_PUBLISH.name());
                    live = false;
                } else {
                    getDocumentRoutingActionBean().startOnlineWorkflow();
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
    public void setOnLineDocumentSelection() {
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
    public void setOffLineDocumentSelection() throws ClientException {
        DocumentModel currentFolder = navigationContext.getCurrentDocument();
        List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

        if (currentDocumentSelection.isEmpty()) {
            return;
        }

        try {
            ToutaticeOperationHelper.runOperationChain(documentManager, ToutaticeNuxeoStudioConst.CST_OPERATION_DOCUMENT_UNPUBLISH_SELECTION,
                    new DocumentModelListImpl(currentDocumentSelection));
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

        if ((null != params) && (params.length > 0)) {
            FacesMessage faceMsg = facesMessages.createFacesMessage(FacesMessage.SEVERITY_INFO, resourcesAccessor.getMessages().get(message), params);
            formattedString = faceMsg.getDetail();
        }

        return formattedString;
    }

    /**
     * <p>Détermine si le document courant appartient à un espace destiné à la
     * publication dans le portail Toutatice </p>
     *
     * @return 'true' si le document courant peut être publié dans le portail.
     *         'false' sinon.
     */
    @Override
    public boolean belongToPublishSpace() {
        return (!ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType().equals(
                ((ToutaticeNavigationContext) navigationContext).getCurrentPublicationArea().getType()));
    }

    /**
     * @return le nom de l'espace de publication dans le portail pour le
     *         document courant
     */
    public String getPublicationAreaNameOfCurrentDocument() {
        return getPublicationAreaName(navigationContext.getCurrentDocument());
    }

    /**
     * <p>Détermine si le document courant appartient à un espace de travail en
     * ligne directe.
     *
     * </p>
     *
     * @return 'true' si le document appartient à une espace de travail. 'false'
     *         sinon.
     */
    @Override
    public boolean belongToWorkSpace() {
        return (!ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType()
                .equals(((ToutaticeNavigationContext) navigationContext).getCurrentWorkspaceArea().getType()));
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
    @Override
    public List<String> getDocumentPathSegments(DocumentModel document, DocumentModel referenceDoc) {
        List<String> list = new ArrayList<String>();

        try {
            if (((null != referenceDoc) && document.getId().equals(referenceDoc.getId()))
                    || (ToutaticeNuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(document.getType()))) {
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


    private String getSpacePath(DocumentModel document) {
        String path = CST_DEFAULT_PUBLICATON_AREA_PATH;
        DocumentModel space = ((ToutaticeNavigationContext) navigationContext).getPublicationArea(document);
        if (ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType().equals(space.getType())) {
            space = navigationContext.getCurrentWorkspace();
        }
        if (null != space) {
            path = space.getPathAsString();
        }
        return path;
    }

    protected String getPublicationAreaName(DocumentModel document) {
        String name = CST_DEFAULT_PUBLICATON_AREA_TITLE;

        DocumentModel area = ((ToutaticeNavigationContext) navigationContext).getPublicationArea(document);
        if (!ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType().equals(area.getType())) {
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
    public DocumentModel getOrCreateChangeableDocument() throws ClientException {
        DocumentModel changeableDocument = navigationContext.getChangeableDocument();

        if ((changeableDocument == null) && (typeName != null)) {
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

            if ((reqParams != null) && !"null".equalsIgnoreCase(reqParams)) {
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
                    throw new ClientException(cnfe.getMessage());
                } catch (IOException ioe) {
                    throw new ClientException(ioe.getMessage());
                }

            }
        }

        return changeableDocument;
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


	public Map<String, Boolean> getMapSwitchState() {
		if(mapSwitchState==null){
			mapSwitchState = new HashMap<String, Boolean>();
		}
		return mapSwitchState;
	}

	public void setMapSwitchState(Map<String, Boolean> mapSwitchState) {
		this.mapSwitchState = mapSwitchState;
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

        if (newDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SPACE_NAVIGATION_ITEM) || "Folder".equals(newDocument.getType())
                || "OrderedFolder".equals(newDocument.getType())) {
            newDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_SIM, true);
            if ("PortalSite".equals(newDocument.getType())) {
                newDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_INTERNAL_CONTEXTUALIZATION, true);
            }
        }
    }

    public boolean showShowInMenu() throws ClientException {
        boolean res = false;
        DocumentModel newDocument = navigationContext.getChangeableDocument();
        if (newDocument == null) {
            newDocument = navigationContext.getCurrentDocument();
        }

        if (newDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SPACE_CONTENT) || "Folder".equals(newDocument.getType())
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
            if ((currentDoc != null) && (param != null)) {
                Object value = currentDoc.getPropertyValue(param);
                if ((value == null) || ((value instanceof String[]) && (((String[]) value).length == 0))) {
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
        if ((newKeyword == null) || "".equals(newKeyword)) {
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
            for (String element : s) {
                subjects.add(element);
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

    public String removeDocumentWebId() throws ClientException {
        DocumentModel currentDoc = getCurrentDocument();
        if (currentDoc != null) {
            currentDoc.setProperty("toutatice", "webid", StringUtils.EMPTY);
        }
        return null;
    }

    /*
     * Service de calcul de permalien vers le portail.
     */
    public PermaLinkService getPermaLinkService() throws ClientException {
        try {
            if (permaLinkService == null) {
                permaLinkService = Framework.getService(PermaLinkService.class);
            }
        } catch (Exception e) {
            log.error("Failed to get the publication service, exception message: " + e.getMessage());
            throw new ClientException("Failed to get the publication service, exception message: " + e.getMessage());
        }
        return permaLinkService;
    }

    @Override
    public String getDocumentPermalink() throws ClientException {
        DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return getDocumentPermalink(currentDoc);

    }

    @Override
    public String getPermalink(String codec){
    	DocumentModel currentDoc = navigationContext.getCurrentDocument();
    	return getPermaLinkService().getPermalink(currentDoc, codec);
    }

    public String getPermalink(DocumentModel doc, String codec){
    	return getPermaLinkService().getPermalink(doc, codec);
    }

    public String getDocumentPermalink(DocumentModel doc) throws ClientException {
        return getPermaLinkService().getPermalink(doc);
    }

    @Override
    public boolean hasChildrenWithType(String type) throws ClientException {
        DocumentModelList docLst = documentManager.getChildren(navigationContext.getCurrentDocument().getRef(), type);
        return ((docLst != null) && !docLst.isEmpty());
    }

    public boolean hasView(String viewId) {
        return ToutaticeDocumentHelper.hasView(navigationContext.getCurrentDocument(), viewId);
    }

	/**
	 * Retourne vrai si le type de document est commentable (contrairement à l'instance de document qui peut ne plus l'avoir).
	 * @return
	 */
    public boolean isTypeCommentable() {
		SchemaManager service = Framework.getService(SchemaManager.class);
		DocumentType documentType = service.getDocumentType(navigationContext.getCurrentDocument().getType());

		return documentType.hasFacet(ToutaticeNuxeoStudioConst.CST_DOC_COMMENTABLE);
    }

    /**
     * Checks if current document belongs to template, i.e. descendant of TemplateRoot.
     * 
     * @return true if current document belongs to template.
     */
    public boolean isCreatingTemplate() {
        boolean is = false; 

        Filter tmplFilter = new Filter(){

            private static final long serialVersionUID = 1L;

            @Override
            public boolean accept(DocumentModel docModel) {
                return docModel != null && StringUtils.equals("TemplateRoot", docModel.getType());
            }
            
        };

        DocumentModelList tmplParents = ToutaticeDocumentHelper.getParentList(this.documentManager, this.navigationContext.getCurrentDocument(), tmplFilter,
                true, true, false);
        if (tmplParents != null && tmplParents.size() == 1) {
            is = true;
        }

        return is;
    }
}