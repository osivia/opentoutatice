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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.helper.VersioningHelper;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeCommentsHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeNotifyEventHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;
import fr.toutatice.ecm.platform.web.fn.WebIdFunctions;

public class ToutaticeCoreProxyWithWorkflowFactory extends CoreProxyWithWorkflowFactory {

    @Override
    public PublishedDocument publishDocument(DocumentModel doc, PublicationNode targetNode, Map<String, String> params) throws ClientException {

        PublishedDocument newPulishedDoc = null;
        DocumentModel newProxy = null;

        if (doc.isProxy()) {

            Map<DocumentModel, List<DocumentModel>> proxyComments = new HashMap<DocumentModel, List<DocumentModel>>();
            proxyComments.putAll(ToutaticeCommentsHelper.getProxyComments(doc));

            newPulishedDoc = super.publishDocument(doc, targetNode, params);

            newProxy = ((SimpleCorePublishedDocument) newPulishedDoc).getProxy();

            ToutaticeCommentsHelper.setComments(super.coreSession, newProxy, proxyComments);

            super.coreSession.saveDocument(newProxy);

        } else {

            newPulishedDoc = super.publishDocument(doc, targetNode, params);
            newProxy = ((SimpleCorePublishedDocument) newPulishedDoc).getProxy();
            
            ToutaticeSilentSaveRSRunner rsRunner = new ToutaticeSilentSaveRSRunner(this.coreSession, doc, targetNode, newPulishedDoc);
            rsRunner.silentRun(false, FILTERED_SERVICES_LIST);

        }

        /* To force ES re-indexing */
        Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
        properties.put("checkedInVersionRef", newProxy.getRef());// FIXME: to avoid nullPointer in AnnotationFulltextEventListener...
        ToutaticeNotifyEventHelper.notifyEvent(super.coreSession, DocumentEventTypes.DOCUMENT_CHECKEDIN, newProxy, new HashMap<String, Serializable>(0));

        return newPulishedDoc;
    }

    private static final List<Class<?>> FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            add(EventService.class);
            add(VersioningService.class);
        }
    };

    /**
     * Allows to save remote sections infos on live document.
     * 
     * @author David Chevrier.
     *
     */
    protected class ToutaticeSilentSaveRSRunner extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel doc;
        private PublicationNode targetNode;
        private PublishedDocument newPulishedDoc;

        public ToutaticeSilentSaveRSRunner(CoreSession session, DocumentModel doc, PublicationNode targetNode, PublishedDocument newPulishedDoc) {
            super(session);
            this.doc = doc;
            this.targetNode = targetNode;
            this.newPulishedDoc = newPulishedDoc;
        }

        /**
         * Save remote sections where live document is published.
         * 
         * @param doc live documentcd
         * @param targetNode remote node section
         */
        @Override
        public void run() throws ClientException {
            DocumentModel remoteSection = this.session.getDocument(new PathRef(targetNode.getPath()));

            if (!this.doc.isFolder()) {

                if (!this.doc.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_HAS_REMOTE_SECTIONS)) {
                    this.doc.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_HAS_REMOTE_SECTIONS);
                }
                
                List<Map<String, Object>> remoteSectionsList = (List<Map<String, Object>>) this.doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_REMOTE_SECTIONS);
                if(remoteSectionsList == null){
                    remoteSectionsList = new ArrayList<Map<String, Object>>();
                }
                
                Map<String, Object> secInfosProperties = new HashMap<String, Object>(3); 
                String rSPath = remoteSection.getPathAsString();

                int rsIndex = ToutaticeWorkflowHelper.getListIndexIfYetPresent(remoteSectionsList, rSPath);
                
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_TITLE_PROP, remoteSection.getTitle());
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_PATH_PROP, rSPath);
                String rsURL = getDocumentURL(remoteSection);
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_URL_PROP, rsURL);
                String proxyURL = getDocumentURL(this.doc);
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_PROXY_URL_PROP, proxyURL);
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_VERSION_PROP, VersioningHelper.getVersionLabelFor(this.doc));
                Boolean pending = Boolean.valueOf(this.newPulishedDoc.isPending());
                secInfosProperties.put(ToutaticeNuxeoStudioConst.CST_DOC_REMOTE_SECTIONS_PENDING_PROP, pending);
                
                if (rsIndex != -1) {
                    /* Override presnet section */
                    remoteSectionsList.remove(rsIndex);
                    remoteSectionsList.add(rsIndex, secInfosProperties);
                } else {
                    remoteSectionsList.add(secInfosProperties); 
                }
                
                this.doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_REMOTE_SECTIONS, (Serializable) remoteSectionsList);
                this.session.saveDocument(this.doc);

            }
        }

    }

    /**
     * @param document
     * @return Nuxeo URL of section.
     */
    protected String getDocumentURL(DocumentModel document){
        String url = StringUtils.EMPTY;
        
        String webid = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
        if (StringUtils.isNotBlank(webid)) {
            url = WebIdFunctions.getPreferredLinkUrl(document);
        } else {
            url = DocumentModelFunctions.documentUrl(document);
        }
        
        if(StringUtils.isNotBlank(url)){
            url = StringUtils.remove(url, BaseURL.getServerURL());
            url = "/" + url;
        }
        
        return url;
    }

}
