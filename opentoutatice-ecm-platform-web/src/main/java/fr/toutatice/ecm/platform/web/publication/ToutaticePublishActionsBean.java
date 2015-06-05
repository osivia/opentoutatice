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
import java.util.Iterator;
import java.util.List;

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
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyNode;
import org.nuxeo.ecm.platform.publisher.impl.service.ProxyTree;
import org.nuxeo.ecm.platform.publisher.web.PublishActionsBean;
import org.richfaces.component.UITree;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;


@Name("publishActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticePublishActionsBean extends PublishActionsBean {

    private static final Log log = LogFactory.getLog(ToutaticePublishActionsBean.class);

    private static final long serialVersionUID = 1L;

    public boolean getCanPublish() throws ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getCanPublish(doc);
    }

    public boolean getCanPublish(DocumentModel document) throws ClientException {
        boolean status = false;

        try {
            if (null != document) {
                PublicationTree tree = getCurrentPublicationTreeForPublishing();
                DocumentModel parent = documentManager.getParentDocument(document.getRef());
                if (null != parent) {
                    status = true;

                    // vérifier qu'il n'existe pas un processus en cours

                    // vérifier que le document n'est pas déjà en ligne avec la
                    // même version
                    DocumentModelList proxies = documentManager.getProxies(document.getRef(), parent.getRef());
                    if ((proxies != null) && (!proxies.isEmpty())) {
                        DocumentModel proxy = proxies.get(0);
                        if (proxy.getVersionLabel().equals(document.getVersionLabel())) {
                            status = false;
                        }
                    }

                    // vérifier que l'arbre de publication autorise la mise en
                    // ligne
                    if (true == status) {
                        PublicationNode target = tree.getNodeByPath(parent.getPathAsString());
                        status = super.canPublishTo(target);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to check the permission to publish the document '" + document.getTitle() + "', error: " + e.getMessage());
        }

        return status;
    }

    public boolean getCanUnpublish() throws ClientException {
        DocumentModel doc = navigationContext.getCurrentDocument();
        return getCanUnpublish(doc);
    }

    public boolean canUnpublishProxy(DocumentModel proxy) throws ClientException {
        boolean status = false;

        /* Proxy non distant */
        if (!isRemoteProxy(proxy)) {
            status = documentManager.hasPermission(proxy.getRef(), ToutaticeNuxeoStudioConst.CST_PERM_VALIDATE);
        } else {
            PublishedDocument publishedDocument = new SimpleCorePublishedDocument(proxy);
            status = super.canUnpublish(publishedDocument);
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
        boolean status = false;

        try {
            List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
            for (DocumentModel selectedDocument : currentDocumentSelection) {
                status = getCanUnpublish(selectedDocument);
                if (false == status) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Faild to check permission to unpublish the selection, error: " + e.getMessage());
        }

        return status;
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
    * @param tree
    * @return true
    */
    public Boolean advisedNodeOpened(UITree tree) {
        return Boolean.TRUE;
    }
    
    /**
     * Service filtered by silent runner.
     */
    private static final List<Class<?>> FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            add(EventService.class);
            add(VersioningService.class);
        }
    };
    
    @Override
    public boolean isPending() throws ClientException {
        return isPending(navigationContext.getCurrentDocument());
    }

    public boolean isPending(DocumentModel document) throws ClientException {
        boolean isPending = false;
        if (document.isProxy()) {
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

}