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
import org.jboss.seam.annotations.intercept.PostActivate;
import org.jboss.seam.annotations.intercept.PrePassivate;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.DocumentType;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.forms.layout.api.FieldDefinition;
import org.nuxeo.ecm.platform.types.SubType;
import org.nuxeo.ecm.platform.types.Type;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActionsBean;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.PortalConstants;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeOperationHelper;
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

    static protected final String CST_DEFAULT_PUBLICATON_AREA_TITLE = "inconnu";

    static protected final String CST_DEFAULT_PUBLICATON_AREA_PATH = "/";

    static protected final String CST_DEFAULT_UNKNOWN_VERSION_LABEL = "Version indéterminée"; // I18N

    @In(create = true)
    protected PageBean pageBean;

    @In(create = true)
    // protected transient PictureBookManager pictureBookManager;

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String addDocumentKeyword() throws NuxeoException {
        if (newKeyword == null || "".equals(newKeyword)) {
            return null;
        }

        // Récupération des la liste des mots-clefs existants
        final DocumentModel currentDoc = getCurrentDocument();
        if (currentDoc != null) {
            final Object subjectsObj = currentDoc.getProperty("toutatice", "keywords");
            List<String> subjects;
            if (subjectsObj != null) {
                if (subjectsObj instanceof List) {
                    subjects = (List) subjectsObj;
                } else {
                    final String[] subjectsArray = (String[]) subjectsObj;
                    subjects = Arrays.asList(subjectsArray);
                    subjects = new ArrayList<>(subjects);
                }
            } else {
                subjects = new ArrayList<>();
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
            final String[] s = newKeyword.split(",");

            // Itération sur la liste des mots-clefs
            for (final String element : s) {
                subjects.add(element);
            }

            newKeyword = "";
            // Affectation de la nouvelle liste à la métadonnée "subjects"
            currentDoc.setProperty("toutatice", "keywords", subjects);
        }
        return null;
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
        for (final FieldDefinition field : fields) {
            try {
                String xpath = field.getPropertyName();

                // handle the case where a specific indexed item is selected
                // within the field value (a list)
                if (xpath.matches(".*/\\d*")) {
                    final String[] xpathElts = xpath.split("/");
                    xpath = xpathElts[0];
                    final int index = Integer.parseInt(xpathElts[1]);

                    fieldValue = doc.getPropertyValue(xpath);
                    final String[] fieldValueList = (String[]) fieldValue;
                    if (fieldValueList.length >= index) {
                        fieldValue = fieldValueList[index];
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
            } catch (final Exception e) {
                try {
                    log.debug("Failed to analyse the document fields (document name: '" + doc.getTitle() + "'), error:" + e.getMessage());
                } catch (final NuxeoException e1) {
                    // ignore
                }
            }
        }

        return status;
    }

    /**
     * Détermine si le document courant appartient à un espace destiné à la
     * publication dans le portail Toutatice </p>
     *
     * @return 'true' si le document courant peut être publié dans le portail.
     *         'false' sinon.
     */
    @Override
    public boolean belongToPublishSpace() {
        return !ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType().equals(
                ((ToutaticeNavigationContext) navigationContext).getCurrentPublicationArea().getType());
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
    @Override
    public boolean belongToWorkSpace() {
        return !ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType()
                .equals(((ToutaticeNavigationContext) navigationContext).getCurrentWorkspaceArea().getType());
    }

    public boolean checkPermission(DocumentModel document, String permission) throws NuxeoException {
        return documentManager.hasPermission(document.getRef(), permission);
    }

    public String createMajorDocument() throws NuxeoException {
        final String viewId = saveDocument();
        updateNUpgradeCurrentDocument("MAJOR");
        return viewId;
    }

    public String createMajorDocument(String viewId) throws NuxeoException {
        createMajorDocument();
        return viewId;
    }

    public String createNSetOnLinePictureBook() throws Exception {
        final String view = "";

        // if (null != pictureBookManager) {
        // // create
        // view = pictureBookManager.createPictureBook();
        //
        // // mise en ligne
        // final DocumentModel newDocument = navigationContext.getCurrentDocument();
        // setDocumentOnline(newDocument);
        // } else {
        log.error("Failed to get the picture book manager from seam context");
        // }

        return view;
    }

    public String createNSetOnLinePictureBook(String viewId) throws Exception {
        // if (null != pictureBookManager) {
        // // create
        // pictureBookManager.createPictureBook();
        // // mise en ligne
        // final DocumentModel newDocument = navigationContext.getCurrentDocument();
        // setDocumentOnline(newDocument);
        // } else {
        log.error("Failed to get the picture book manager from seam context");
        // }

        return viewId;
    }

    public String createPictureBook(String viewId) throws Exception {
        // if (null != pictureBookManager) {
        // // create
        // pictureBookManager.createPictureBook();
        // pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name());
        // live = true;
        // } else {
        log.error("Failed to get the picture book manager from seam context");
        // }

        return viewId;
    }

    @Destroy
    // @Remove
    @PermitAll
    public void destroy() {
        log.debug("Removing SEAM action listener...");
    }

    private String extractExtension(String fileName) throws NuxeoException {
        final String[] parts = fileName.split("\\.");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "none";
    }

    @Factory(value = "currentEditedDocument", scope = EVENT)
    public DocumentModel factoryCurrentEditedDocument() {
        return navigationContext.getCurrentDocument();
    }

    @SuppressWarnings("static-access")
    public String formatMessages(String message, Object... params) throws NuxeoException {
        String formattedString = "";

        if (null != params && params.length > 0) {
            final FacesMessage faceMsg = facesMessages.createFacesMessage(FacesMessage.SEVERITY_INFO, resourcesAccessor.getMessages().get(message), params);
            formattedString = faceMsg.getDetail();
        }

        return formattedString;
    }

    /**
     * Return the current document being either edited or created
     *
     * @return the current document
     * @throws NuxeoException
     */
    public DocumentModel getCurrentDocument() throws NuxeoException {
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

    /**
     * @return le nom de l'espace de publication dans le portail pour le
     *         document courant
     */
    // TODO a déplacer dans le addon publication distante
    @Override
    public List<String> getDocumentPathSegments(DocumentModel document, DocumentModel referenceDoc) {
        final List<String> list = new ArrayList<>();

        try {
            if (null != referenceDoc && document.getId().equals(referenceDoc.getId())
                    || ToutaticeNuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(document.getType())) {
                return list;
            }

            list.add(document.getTitle());

            final DocumentModel parent = documentManager.getParentDocument(document.getRef());
            list.addAll(getDocumentPathSegments(parent, referenceDoc));
        } catch (final NuxeoException e) {
            log.error("Failed to get the path segments of document '" + document.getName() + "', error: " + e.getMessage());
        }

        return list;
    }

    @Override
    public String getDocumentPermalink() throws NuxeoException {
        final DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return getDocumentPermalink(currentDoc);

    }

    public String getDocumentPermalink(DocumentModel doc) throws NuxeoException {
        return getPermaLinkService().getPermalink(doc);
    }

    protected ToutaticeDocumentRoutingActionsBean getDocumentRoutingActionBean() {
        return (ToutaticeDocumentRoutingActionsBean) SeamComponentCallHelper.getSeamComponentByName("routingActions");
    }

    public boolean getEveryThingRight() throws NuxeoException {
        return documentManager.hasPermission(navigationContext.getCurrentDocument().getRef(), SecurityConstants.EVERYTHING);
    }

    public Map<String, Boolean> getMapSwitchState() {
        if(mapSwitchState==null){
            mapSwitchState = new HashMap<>();
        }
        return mapSwitchState;
    }

    public String getNewKeyword() {
        return newKeyword;
    }

    public String getNewSwitchValue() {
        return newSwitchValue;
    }

    /**
     * Récupèrer le document de création (ChangeableDocument) ou le créer s'il
     * n'existe pas.
     *
     * @return le document nouvellement créé
     */
    public DocumentModel getOrCreateChangeableDocument() throws NuxeoException {
        DocumentModel changeableDocument = navigationContext.getChangeableDocument();

        if (changeableDocument == null && typeName != null) {
            // vérifier que le document peut être créé dans le document courant
            // parent/containeur
            final DocumentModel container = navigationContext.getCurrentDocument();
            final Type cType = typeManager.getType(container.getType());
            final Map<String, SubType> allowedSubTypes = cType.getAllowedSubTypes();

            if (allowedSubTypes.containsKey(typeName)) {
                // créer le changeable document du type réclamé
                createDocument(typeName);
                changeableDocument = navigationContext.getChangeableDocument();

            } else {
                return null;
            }

            if (reqParams != null && !"null".equalsIgnoreCase(reqParams)) {
                try {

                    final Base64 b64 = new Base64();
                    final byte[] tabByte = b64.decode(reqParams.getBytes());
                    final InputStream io = new ByteArrayInputStream(tabByte);

                    final ObjectInputStream ois = new ObjectInputStream(io);
                    final Object o = ois.readObject();
                    Map<String, List<String>> params = new HashMap<>();
                    if (o instanceof Map) {
                        params = (Map<String, List<String>>) o;
                    }
                    for (final String xpath : params.keySet()) {
                        changeableDocument.setPropertyValue(xpath, (Serializable) params.get(xpath));
                    }
                } catch (final ClassNotFoundException cnfe) {
                    throw new NuxeoException(cnfe.getMessage());
                } catch (final IOException ioe) {
                    throw new NuxeoException(ioe.getMessage());
                }

            }
        }

        return changeableDocument;
    }

    public DocumentModel getParent(DocumentModel document, boolean unrestricted){
        DocumentModel parent = null;

        final DocumentModelList parentList = ToutaticeDocumentHelper.getParentList(documentManager, document, null, unrestricted, true);
        if(CollectionUtils.isNotEmpty(parentList)){
            parent = parentList.get(0);
        }

        return parent;
    }

    public String getPermalink(DocumentModel doc, String codec){
        return getPermaLinkService().getPermalink(doc, codec);
    }

    @Override
    public String getPermalink(String codec){
        final DocumentModel currentDoc = navigationContext.getCurrentDocument();
        return getPermaLinkService().getPermalink(currentDoc, codec);
    }

    /*
     * Service de calcul de permalien vers le portail.
     */
    public PermaLinkService getPermaLinkService() throws NuxeoException {
        try {
            if (permaLinkService == null) {
                permaLinkService = Framework.getService(PermaLinkService.class);
            }
        } catch (final Exception e) {
            log.error("Failed to get the publication service, exception message: " + e.getMessage());
            throw new NuxeoException("Failed to get the publication service, exception message: " + e.getMessage());
        }
        return permaLinkService;
    }

    @Override
    public DocumentModel getProxy(DocumentModel document) throws NuxeoException {
        return ToutaticeDocumentHelper.getProxy(documentManager, document, SecurityConstants.READ);
    }

    @Override
    public String getProxyVersion(DocumentModel document) throws NuxeoException {
        final String proxyVersion = ToutaticeDocumentHelper.getProxyVersion(documentManager, document);
        return StringUtils.isNotBlank(proxyVersion) ? proxyVersion : CST_DEFAULT_UNKNOWN_VERSION_LABEL;
    }

    protected String getPublicationAreaName(DocumentModel document) {
        String name = CST_DEFAULT_PUBLICATON_AREA_TITLE;

        final DocumentModel area = ((ToutaticeNavigationContext) navigationContext).getPublicationArea(document);
        if (!ToutaticeGlobalConst.NULL_DOCUMENT_MODEL.getType().equals(area.getType())) {
            try {
                name = area.getTitle();
            } catch (final NuxeoException e) {
                log.debug("Failed to get the publication area name, error: " + e.getMessage());
            }
        }

        return name;
    }

    /**
     * @return le nom de l'espace de publication dans le portail pour le
     *         document courant
     */
    public String getPublicationAreaNameOfCurrentDocument() {
        return getPublicationAreaName(navigationContext.getCurrentDocument());
    }

    public String getSource() {
        return source;
    }

    /**
     * @return le nom de l'espace de publication dans le portail pour le
     *         document courant
     */
    public String getSpacePath() {
        return getSpacePath(navigationContext.getCurrentDocument());
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

    @Override
    public boolean hasChildrenWithType(String type) throws NuxeoException {
        final DocumentModelList docLst = documentManager.getChildren(navigationContext.getCurrentDocument().getRef(), type);
        return docLst != null && !docLst.isEmpty();
    }

    /**
     * @return true if the current document owns a proxy (is online)
     * @throws NuxeoException
     */
    @Override
    public boolean hasProxy(DocumentModel document) throws NuxeoException {
        return null != getProxy(document);
    }

    public boolean hasView(String viewId) {
        return ToutaticeDocumentHelper.hasView(navigationContext.getCurrentDocument(), viewId);
    }

    @Create
    public void initialize() throws Exception {
        log.debug("Initializing...");
    }

    @Observer(value = {EventNames.NEW_DOCUMENT_CREATED})
    public void initShowInMenu() throws NuxeoException {
        final DocumentModel newDocument = navigationContext.getChangeableDocument();

        if (newDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_SPACE_NAVIGATION_ITEM) || "Folder".equals(newDocument.getType())
                || "OrderedFolder".equals(newDocument.getType())) {
            newDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_SIM, true);
            if ("PortalSite".equals(newDocument.getType())) {
                newDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_INTERNAL_CONTEXTUALIZATION, true);
            }
        }
    }

    public boolean initSwitchState(String param) throws PropertyException, NuxeoException {
        if (null == mapSwitchState) {
            mapSwitchState = new HashMap<>();
        }

        if (mapSwitchState.get(param) == null) {

            final DocumentModel currentDoc = getCurrentDocument();
            if (currentDoc != null && param != null) {
                final Object value = currentDoc.getPropertyValue(param);
                if (value == null || value instanceof String[] && ((String[]) value).length == 0) {
                    mapSwitchState.put(param, true);
                } else {
                    mapSwitchState.put(param, false);
                }
            }
        }

        return mapSwitchState.get(param);
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
                final String fieldValueStg = (String) fieldValue;
                if (StringUtils.isNotBlank(fieldValueStg)) {
                    status = false;
                }
            } else if (fieldValue instanceof String[]) {
                final String[] fieldValuesList = (String[]) fieldValue;
                if (fieldValuesList.length > 0) {
                    for (final String item : fieldValuesList) {
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

    public boolean isFileImage(String fileName) throws NuxeoException {
        return extractExtension(fileName).equalsIgnoreCase("gif") || extractExtension(fileName).equalsIgnoreCase("jpeg")
                || extractExtension(fileName).equalsIgnoreCase("jpg") || extractExtension(fileName).equalsIgnoreCase("png");
    }

    public boolean isFileMp3Playable(String fileName) throws NuxeoException {
        return extractExtension(fileName).equalsIgnoreCase("mp3");
    }

    /**
     * @return true if given document is in Publish Space.
     */
    public boolean isInPublishSpace(DocumentModel document){
        return ToutaticeDocumentHelper.isInPublishSpace(documentManager, document);
    }

    public boolean isLive() {
        return live;
    }

    private boolean isNewlyCreatedChangeableDocument(DocumentModel changeableDocument) throws NuxeoException {
        return changeableDocument != null && changeableDocument.getId() == null && changeableDocument.getPath() == null && changeableDocument.getTitle() == null;
    }

    public boolean isOnlineDocument() throws NuxeoException {
        final DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isOnlineDocument(currentDocument);
    }

    /**
     * Indique si le document courant possède une version en ligne
     *
     * @param document
     * @return true si le document courant possède une version en ligne. false
     *         sinon.
     * @throws NuxeoException
     */
    public boolean isOnlineDocument(DocumentModel document) throws NuxeoException {
        boolean res = false;
        try{
            if(ToutaticeDocumentHelper.isDocStillExists(documentManager, document)){
                res = hasProxy(document);
            }
        }catch(final DocumentSecurityException se){
            // dans le cadre de la publication profilée
            // re-visualisation d'un contentview contenant un document qui n'est plus visible par l'utilisateur courant
            // le document live est encore dans le cache par contre son proxy est mis à jour est n'est plus visible par l'U
            log.warn(se.getMessage());
        }
        return res;
    }

    public boolean isOnlineWithSameVersion() {
        final DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return isOnlineWithSameVersion(currentDocument);
    }

    private boolean isOnlineWithSameVersion(DocumentModel document) {
        boolean status = false;

        try {
            final String onlineDocVersion = getProxyVersion(document);
            if (document.getVersionLabel().equals(onlineDocVersion)) {
                status = true;
            }
        } catch (final Exception e) {
            log.debug("Failed to execute 'isOnlineWithSameVersion', error: " + e.getMessage());
        }

        return status;
    }

    public boolean isRemoteProxy() {
        final DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return currentDocument.isProxy() && !StringUtils.endsWith(currentDocument.getName(), ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
    }

    /**
     * @param currentDocument
     * @return true if current document is remote proxy.
     */
    public boolean isRemoteProxy(DocumentModel currentDocument) {
        return currentDocument.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY);
    }

    /**
     * Determine si l'action "seeLiveDocumentVersion" de la vue 'summary' doit être présentée.
     *
     * <h4>Conditions</h4> <li>le document visualisé ne doit pas être la version live</li> <li>l'usager connecté doit avoir au minima un droit de lecture sur le
     * doucment source</li>
     *
     * @return true si l'action doit être présentée. false sinon.
     * @throws NuxeoException
     */
    public boolean isSeeLiveDocumentVersionActionAuthorized() {
        boolean status = false;
        DocumentModel currentDoc = null;

        try {
            currentDoc = navigationContext.getCurrentDocument();
            final DocumentModel sourceVersionDoc = documentManager.getSourceDocument(currentDoc.getRef());
            final DocumentModel workingCopy = documentManager.getWorkingCopy(sourceVersionDoc.getRef());
            final boolean isDeleted = workingCopy == null
                    || workingCopy != null && LifeCycleConstants.DELETED_STATE.equals(workingCopy.getCurrentLifeCycleState());
            if (!isDeleted && !sourceVersionDoc.getId().equals(currentDoc.getId())) {
                if (documentManager.hasPermission(sourceVersionDoc.getRef(), SecurityConstants.READ)) {
                    status = true;
                }
            }
        } catch (final Exception e) {
            final String docName = null != currentDoc ? currentDoc.getName() : "unknown";
            log.debug("Failed to check the status to see the live version of the document '" + docName + "', error: " + e.getMessage());
        }

        return status;
    }

    /**
     * Determine si l'action "seeOnlineDocumentVersion" doit être présentée.
     *
     * <h4>Conditions</h4> <li>le document doit posséder un proxy local (publication locale pour mise en ligne)</li> <li>le document ne doit pas être la version
     * valide</li>
     *
     * @return true si l'action doit être présentée. false sinon.
     * @throws NuxeoException
     */
    public boolean isSeeOnlineDocumentVersionActionAuthorized() {
        boolean status = false;
        DocumentModel currentDoc = null;

        // vérifie si un proxy existe
        try {
            currentDoc = navigationContext.getCurrentDocument();
            final String proxyVersionLabel = getProxyVersion(currentDoc);
            if (!CST_DEFAULT_UNKNOWN_VERSION_LABEL.equals(proxyVersionLabel)) {
                status = !currentDoc.getVersionLabel().equals(proxyVersionLabel);
            }
        } catch (final Exception e) {
            final String docName = null != currentDoc ? currentDoc.getName() : "unknown";
            log.debug("Failed to check the online status of the document '" + docName + "', error: " + e.getMessage());
        }

        return status;
    }

    /**
     * Retourne vrai si le type de document est commentable (contrairement à l'instance de document qui peut ne plus l'avoir).
     * @return
     */
    public boolean isTypeCommentable() {
        final SchemaManager service = Framework.getService(SchemaManager.class);
        final DocumentType documentType = service.getDocumentType(navigationContext.getCurrentDocument().getType());

        return documentType.hasFacet(ToutaticeNuxeoStudioConst.CST_DOC_COMMENTABLE);
    }

    public String navigateToView(String viewId) throws NuxeoException {

        final DocumentModel doc = navigationContext.getCurrentDocument();
        return navigationContext.navigateToDocument(doc, viewId);
    }

    /**
     * Mettre en ligne une sélection de documents dans un content view (folder
     * ou document non folderish)
     */
    public void publishDocumentSelection() {
        final List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

        if (currentDocumentSelection.isEmpty()) {
            return;
        }

        for (final DocumentModel selectedDocument : currentDocumentSelection) {
            // publication d'un document
            setDocumentOnline(selectedDocument);
        }

        // Rafraîchir la liste de sélections
        documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);

        // Rafraîchir le content view
        final DocumentModel currentFolder = navigationContext.getCurrentDocument();
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED, currentFolder);
    }

    @Observer(value = {EventNames.DOCUMENT_SELECTION_CHANGED, EventNames.NEW_DOCUMENT_CREATED})
    public Map<String, Boolean> razMapSwitchState() {
        if (null == mapSwitchState) {
            mapSwitchState = new HashMap<>();
        } else {
            mapSwitchState.clear();
        }
        return mapSwitchState;
    }

    @PostActivate
    public void readState() {
        log.debug("PostActivate");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String removeDocumentKeyword() throws NuxeoException {
        final DocumentModel currentDoc = getCurrentDocument();
        if (currentDoc != null) {
            final Object subjectsObj = currentDoc.getProperty("toutatice", "keywords");
            List<String> subjects;
            if (subjectsObj instanceof List) {
                subjects = (List) subjectsObj;
            } else {
                final String[] subjectsArray = (String[]) subjectsObj;
                subjects = Arrays.asList(subjectsArray);
                subjects = new ArrayList<>(subjects);
            }
            final FacesContext context = FacesContext.getCurrentInstance();
            final String subject = context.getExternalContext().getRequestParameterMap().get("subject");
            subjects.remove(subject);
            currentDoc.setProperty("toutatice", "keywords", subjects);
        }
        return null;
    }

    @Observer(value = {EventNames.DOCUMENT_SELECTION_CHANGED})
    public void resetChgDocument() {
        navigationContext.setChangeableDocument(null);
    }


    @Override
    public String saveDocument() throws NuxeoException {
        final DocumentModel changeableDocument = navigationContext.getChangeableDocument();
        updateDocWithMapSwitch(changeableDocument);

        return super.saveDocument(changeableDocument);
    }

    public String saveDocument(String viewId) throws NuxeoException {
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

    public String saveNSetOnLineDocument() throws NuxeoException {
        // sauvegarde
        final DocumentModel changeableDocument = navigationContext.getChangeableDocument();
        updateDocWithMapSwitch(changeableDocument);
        final String view = super.saveDocument(changeableDocument);
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name());

        // mise en ligne
        final DocumentModel newDocument = navigationContext.getCurrentDocument();
        setDocumentOnline(newDocument);

        return view;
    }

    public String saveNSetOnLineDocument(String viewId) throws NuxeoException {
        saveNSetOnLineDocument();
        return viewId;
    }

    @PrePassivate
    public void saveState() {
        log.debug("PrePassivate");
    }

    protected void setDocumentOnline(DocumentModel document) {
        try {
            final String proxyVersionLabel = getProxyVersion(document);
            if (!document.getVersionLabel().equals(proxyVersionLabel)) {
                if (documentManager.hasPermission(document.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE)) {
                    ToutaticeOperationHelper.runOperationChain(documentManager, ToutaticeNuxeoStudioConst.CST_OPERATION_DOCUMENT_PUBLISH_ONLY, document);
                    pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_PUBLISH.name());
                    live = false;
                } else {
                    getDocumentRoutingActionBean().startOnlineWorkflow();
                }
            }
        } catch (final Exception e) {
            log.error("Failed to set online the document: '" + document.getName() + "', error: " + e.getMessage());
        }
    }


    public void setLive(boolean live) {
        this.live = live;
    }

    public void setMapSwitchState(Map<String, Boolean> mapSwitchState) {
        this.mapSwitchState = mapSwitchState;
    }


    public void setNewKeyword(String newKeyword) {
        this.newKeyword = newKeyword;
    }

    public void setNewSwitchValue(String newSwitchValue) {
        this.newSwitchValue = newSwitchValue;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean showShowInMenu() throws NuxeoException {
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

    /**
     * Mettre hors ligne une sélection de documents dans un content view (folder
     * ou document non folderish)
     *
     * @throws NuxeoException
     */
    public void unPublishDocumentSelection() throws NuxeoException {
        final DocumentModel currentFolder = navigationContext.getCurrentDocument();
        final List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);

        if (currentDocumentSelection.isEmpty()) {
            return;
        }

        try {
            ToutaticeOperationHelper.runOperationChain(documentManager, ToutaticeNuxeoStudioConst.CST_OPERATION_DOCUMENT_UNPUBLISH_SELECTION,
                    new DocumentModelListImpl(currentDocumentSelection));
        } catch (final Exception e) {
            log.error("Failed to set offline the selection from the document: '" + currentFolder.getTitle() + "', error: " + e.getMessage());
        }

        // Rafraîchir la liste de sélections
        documentsListsManager.resetWorkingList(CURRENT_DOCUMENT_SELECTION);

        // Rafraîchir le content view
        Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED, currentFolder);
    }

    /**
     * surcharge de la méthode updateCurrentDocument de DocumentActionsBean pour
     * faire la mise à jour en fonction de mapSwitchState
     */
    @Override
    public String updateCurrentDocument() throws NuxeoException {
        final DocumentModel currentDocument = navigationContext.getCurrentDocument();
        updateDocWithMapSwitch(currentDocument);
        final String viewId = super.updateDocument(currentDocument);
        return viewId;
    }

    public String updateCurrentDocument(String viewId) throws NuxeoException {
        updateCurrentDocument();
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_MODIFY.name());
        live = true;
        return viewId;
    }

    @Override
    public String updateDocument(DocumentModel doc) throws NuxeoException{
        return super.updateDocument(doc);
    }

    /**
     * méthode permettant de prendre en compte les éléments de map
     * mapSwitchState
     *
     * @param document
     *            document à mettre à jour
     * @throws PropertyException
     * @throws NuxeoException
     */
    @Override
    public void updateDocWithMapSwitch(DocumentModel document) throws PropertyException, NuxeoException {
        if (null == mapSwitchState) {
            return;
        }

        for (final String key : mapSwitchState.keySet()) {

            if (mapSwitchState.get(key)) {
                document.setPropertyValue(key, null);
            }
        }
    }

    public String updateNSetOnLineCurrentDocument() throws NuxeoException {
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

    public String updateNSetOnLineCurrentDocument(String viewId) throws NuxeoException {
        updateNSetOnLineCurrentDocument();
        return viewId;
    }

    /**
     * mise à jour et incrémentation de la version(MINOR ou MAJOR) du document courant
     *
     * @param version MINOR ou MAJOR
     * @return l'identifiant de la vue retour
     * @throws NuxeoException
     */
    @Override
    public String updateNUpgradeCurrentDocument(String version) throws NuxeoException {
        final DocumentModel currentDocument = navigationContext.getCurrentDocument();
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
     * @throws NuxeoException
     */
    public String updateNUpgradeCurrentDocument(String version, String viewId) throws NuxeoException {
        updateNUpgradeCurrentDocument(version);
        pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_MODIFY.name());
        live = true;
        return viewId;
    }

    public String viewLiveVersion() throws NuxeoException {
        String output = "";

        final DocumentModel currentDoc = navigationContext.getCurrentDocument();
        try {
            final DocumentModel sourceVersionDoc = documentManager.getSourceDocument(currentDoc.getRef());
            if (null != sourceVersionDoc) {
                final DocumentModel liveDoc = documentManager.getSourceDocument(sourceVersionDoc.getRef());
                if (null != liveDoc) {
                    output = navigationContext.navigateToDocument(liveDoc);
                }
            }
        } catch (final NuxeoException e) {
            log.info("The proxy document (' " + currentDoc.getName() + "') has lost its reference to the version document");
            facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor.getMessages().get("label.toutatice.viewlive.reference.lost"));
        }

        return output;
    }

    public String viewOnlineVersion() throws NuxeoException {
        return viewOnlineVersion(navigationContext.getCurrentDocument());
    }

    public String viewOnlineVersion(DocumentModel document) throws NuxeoException {
        String output = "";

        try {
            final DocumentModel proxy = getProxy(document);
            if (null != proxy) {
                final String srcDocId = proxy.getSourceId();
                final DocumentModel srcDoc = documentManager.getDocument(new IdRef(srcDocId));
                output = navigationContext.navigateToDocument(document, ToutaticeDocumentHelper.getVersionModel(srcDoc));
            }
        } catch (final DocumentException e) {
            throw new NuxeoException(e);
        }

        return output;
    }
}