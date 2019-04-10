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
package fr.toutatice.ecm.platform.web.publication;

import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublicationTreeNotAvailable;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyNode;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyTree;
import org.nuxeo.ecm.platform.publisher.web.PublishActionsBean;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.webapp.action.DeleteActions;
import org.richfaces.component.UITree;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;
import fr.toutatice.ecm.platform.web.publication.finder.ToutaticeRootSectionsFinder;


@Name("publishActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticePublishActionsBean extends PublishActionsBean {

    private static final Log log = LogFactory.getLog(ToutaticePublishActionsBean.class);

    private static final long serialVersionUID = 1L;

    private ToutaticeDocumentActionsBean documentActions;
    private DeleteActions deleteActions;

    protected ToutaticeDocumentActionsBean getToutaticeDocumentActionBean() {
        if (this.documentActions == null) {
            this.documentActions = (ToutaticeDocumentActionsBean) SeamComponentCallHelper.getSeamComponentByName("documentActions");
        }
        return this.documentActions;
    }

    protected DeleteActions getDeleteActions() {
        if (this.deleteActions == null) {
            this.deleteActions = (DeleteActions) SeamComponentCallHelper.getSeamComponentByName("deleteActions");
        }
        return this.deleteActions;
    }

    public boolean getCanPublish() throws ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getCanPublish(doc);
    }

    public boolean getCanPublish(DocumentModel document) throws ClientException {
        boolean can = false;

        try {
            if (document != null) {
                // Is document in PublishSpace
                if (ToutaticeDocumentHelper.isInPublishSpace(this.documentManager, document)) {

                    // Check if can set online
                    boolean canWrite = this.documentManager.hasPermission(document.getRef(), SecurityConstants.WRITE);
                    boolean canPublish = this.documentManager.hasPermission(document.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE);
                    // Status
                    can = canWrite && canPublish;

                    if (can) {
                        // vérifier qu'il n'existe pas un processus en cours
                        // TODO

                        // Check if document is not online with same version
                        DocumentRef parentRef = document.getParentRef();
                        DocumentModelList proxies = this.documentManager.getProxies(document.getRef(), parentRef);
                        if ((proxies != null) && (!proxies.isEmpty())) {
                            DocumentModel proxy = proxies.get(0);
                            if (proxy.getVersionLabel().equals(document.getVersionLabel())) {
                                can = false;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to check the permission to publish the document '" + document.getTitle() + "', error: " + e.getMessage());
        }

        return can;
    }

    public boolean getCanUnpublish() throws ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getCanUnpublish(doc);
    }

    public boolean canUnpublishProxy(DocumentModel proxy) throws ClientException {
        boolean status = false;

        // For local proxies only
        if (proxy != null && proxy.isProxy() && !proxy.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)) {
            status = documentManager.hasPermission(proxy.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE);
        }

        return status;
    }

    public boolean getCanUnpublish(DocumentModel document) throws ClientException {
        boolean status = false;

        try {
            if (null != document) {
                DocumentRef parentRef = documentManager.getParentDocumentRef(document.getRef());
                if (null != parentRef) {
                    DocumentModelList proxies = documentManager.getProxies(document.getRef(), parentRef);
                    if ((proxies != null) && (!proxies.isEmpty())) {
                        DocumentModel proxy = proxies.get(0);
                        status = canUnpublishProxy(proxy);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to check the permission to unpublish the document '" + document.getTitle() + "', error: " + e.getMessage());
        }

        return status;
    }
    
    public boolean getCanPublishSelection() {
        boolean status = false;

        try {
            List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
            for (DocumentModel selectedDocument : currentDocumentSelection) {
                status = getCanPublish(selectedDocument);
                if (false == status) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Failed to check permission to publish the selection, error: " + e.getMessage());
        }

        return status;
    }

    public boolean getCanUnPublishSelection() {
        boolean can = false;

        List<DocumentModel> currentDocumentSelection = this.documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
        if (CollectionUtils.isNotEmpty(currentDocumentSelection)) {
            // Split lis in in Toutatice local proxies & Nx remote proxies lists
            List<DocumentModel> lcPxs = new ArrayList<>();
            List<DocumentModel> rmPxs = new ArrayList<>();

            for (DocumentModel pxy : currentDocumentSelection) {
                if (pxy.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)) {
                    rmPxs.add(pxy);
                } else {
                    lcPxs.add(pxy);
                }
            }

            try {
                // Toutatice local proxies treatment
                boolean canLcPxs = true;
                if (CollectionUtils.isNotEmpty(lcPxs)) {
                    Iterator<DocumentModel> itLcPxy = lcPxs.iterator();

                    while (itLcPxy.hasNext() && canLcPxs) {
                        DocumentModel lcPxy = itLcPxy.next();
                        canLcPxs = getCanUnpublish(lcPxy);
                    }

                    can = canLcPxs;
                }

                // Nx remote proxies
                boolean canRmPxs = true;
                if (canLcPxs && CollectionUtils.isNotEmpty(rmPxs)) {
                    if (this.getDeleteActions().checkDeletePermOnParents(rmPxs)) {
                        Iterator<DocumentModel> itRmPxs = rmPxs.iterator();

                        while (itRmPxs.hasNext() && canRmPxs) {
                            DocumentModel rmPxy = itRmPxs.next();
                            canRmPxs = !(rmPxy.hasFacet(FacetNames.PUBLISH_SPACE) || rmPxy.hasFacet(FacetNames.MASTER_PUBLISH_SPACE));
                        }
                    } else {
                        canRmPxs = false;
                    }

                    can = canRmPxs;
                }
            } catch (Exception e) {
                log.error("Failed to check permission to publish the selection, error: " + e.getMessage());
            }
        }

        return can;
    }

    public boolean isRemoteProxy(DocumentModel proxy) {
        return proxy.isProxy() && !StringUtils.endsWith(proxy.getName(), ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
    }

    @Override
    protected void getPathFragments(DocumentModel document, List<String> pathFragments) throws ClientException {
        // ajouter le nom du document courant
        pathFragments.add(document.getTitle());

        // récupération du SectionRoot
        DocumentModel sectionRoot = ((ToutaticeNavigationContext) navigationContext).getSectionPublicationArea(document);
        if (!sectionRoot.equals(document)) {
            pathFragments.add(sectionRoot.getTitle());
        }

        // récupérer le nom du domaine associé au document
        DocumentModel domain = ((ToutaticeNavigationContext) navigationContext).getDocumentDomain(document);
        pathFragments.add(domain.getTitle());
    }

    public String getIconPath(Object node) throws ClientException {
        String iconPath = "";
        if (node instanceof ProxyNode) {
            String path = ((ProxyNode) node).getPath();
            PathRef pathRef = new PathRef(path);
            DocumentModel document = documentManager.getDocument(pathRef);
            iconPath = (String) document.getProperty("common", "icon");
        }
        if (node instanceof ProxyTree) {
            iconPath = "/icons/domain.gif";
        }
        return iconPath;
    }

    /**
     * To expand by default all nodes of Rich Tree
     * 
     * @param tree
     * @return true
     */
    public Boolean advisedNodeOpened(UITree tree) {
        return Boolean.TRUE;
    }

    @Override
    public boolean isPending() throws ClientException {
        return isPending(navigationContext.getCurrentDocument());
    }

    public boolean isPending(DocumentModel document) throws ClientException {
        boolean isPending = false;
        if (ToutaticeDocumentHelper.isDocStillExists(documentManager, document) && document.isProxy()) {
            PublicationTree tree = publisherService.getPublicationTreeFor(document, documentManager);
            PublishedDocument publishedDocument = tree.wrapToPublishedDocument(document);
            isPending = publishedDocument.isPending();
        }
        return isPending;
    }

    public boolean hasRemoteProxy() throws ClientException {
        boolean has = false;
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        DocumentModelList proxies = documentManager.getProxies(currentDocument.getRef(), null);
        if (proxies != null && proxies.size() > 0) {
            Iterator<DocumentModel> iterator = proxies.iterator();
            while (iterator.hasNext() && !has) {
                DocumentModel proxy = iterator.next();
                if (isRemoteProxy(proxy)) {
                    has = true;
                }
            }
        }
        return has;
    }

    /**
     * Type of tree changed (RootSectionsPublicationTree to ToutaticeRootSectionsPublicationTree)
     * to be coherent with publisher-task-contrib.xml.
     */
    @Override
    protected List<String> filterEmptyTrees(Collection<String> trees) throws PublicationTreeNotAvailable, ClientException {
        List<String> filteredTrees = new ArrayList<>();

        ToutaticeRootSectionsFinder finder = new ToutaticeRootSectionsFinder(documentManager);
        DocumentModelList accessibleSections = finder.getAccessibleSectionRoots(navigationContext.getCurrentDocument());

        for (String tree : trees) {

            try {
                // Tree Domain
                String treeDomain = StringUtils.substringAfter(tree, "-");

                boolean inDomain = false;
                for (DocumentModel section : accessibleSections) {
                    if (section.getPathAsString().startsWith("/" + treeDomain))
                        inDomain = true;
                }

                if (inDomain) {

                    if (log.isDebugEnabled()) {
                        log.debug("#filterEmptyTrees: == [LOOP: Selected tree]: " + tree);
                    }

                    PublicationTree pTree = publisherService.getPublicationTree(tree, documentManager, null, navigationContext.getCurrentDocument());
                    if (pTree != null) {
                        if (pTree.getTreeType().equals("ToutaticeRootSectionsPublicationTree")) {
                            if (pTree.getChildrenNodes().size() > 0) {
                                filteredTrees.add(tree);
                            }
                        } else {
                            filteredTrees.add(tree);
                        }
                    }
                }
            } catch (PublicationTreeNotAvailable e) {
                log.warn("Publication tree " + tree + " is not available : check config");
                log.debug("Publication tree " + tree + " is not available : root cause is ", e);
            }
        }
        return filteredTrees;
    }

}