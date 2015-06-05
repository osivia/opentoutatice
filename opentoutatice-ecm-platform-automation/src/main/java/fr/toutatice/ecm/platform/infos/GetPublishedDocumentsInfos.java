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
 * dchevrier
 */
package fr.toutatice.ecm.platform.infos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.publisher.api.PublicationTree;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.api.PublisherService;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.service.url.ToutaticeDocumentLocation;


/**
 * @author David Chevrier.
 *
 */
@Operation(id = GetPublishedDocumentsInfos.ID, category = Constants.CAT_DOCUMENT, label = "GetPublishedDocumentsInfos",
        description = "Get informations of remote published documents of a given live document.")
public class GetPublishedDocumentsInfos {

    public static final String ID = "Document.GetPublishedDocumentsInfos";

    @Context
    protected CoreSession session;

    @Context
    protected PublisherService publisherService;

    @OperationMethod
    public StringBlob run(DocumentModel document) throws Exception {

        JSONArray informations = new JSONArray();

        if (!document.isProxy()) {

            Map<String, String> availablePublicationTrees = publisherService.getAvailablePublicationTrees();

            if (MapUtils.isNotEmpty(availablePublicationTrees)) {
                for (Entry<String, String> treeInfo : availablePublicationTrees.entrySet()) {
                    String treeName = treeInfo.getKey();

                    PublicationTree tree = publisherService.getPublicationTree(treeName, this.session, null);
                    List<PublishedDocument> publishedDocuments = tree.getExistingPublishedDocument(new DocumentLocationImpl(document));

                    JSONObject documentInfos = new JSONObject();
                    for (PublishedDocument publishedDocument : publishedDocuments) {

                        DocumentModel proxy = ((SimpleCorePublishedDocument) publishedDocument).getProxy();
                        documentInfos.element("url", getDocumentURL(proxy));

                        DocumentModel parentDocument = this.session.getParentDocument(proxy.getRef());
                        documentInfos.element("sectionTitle", parentDocument.getTitle());
                        
                        informations.add(documentInfos);
                    }

                }

            }
        }

        return new StringBlob(informations.toString(), "application/json");

    }

    public static final String WEBID_PATTERN = "webidpattern";
    public static final String WEB_APP = "/nuxeo/";

    /**
     * @param document
     * @return Nuxeo URL of document with webid.
     *         If not, URL is enmpty.
     */
    protected String getDocumentURL(DocumentModel document) {
        String url = StringUtils.EMPTY;

        if (document != null) {
            url = DocumentModelFunctions.documentUrl(document);

            if (StringUtils.isNotBlank(url)) {
                url = WEB_APP + url;
            }
        }

        return url;
    }

}
