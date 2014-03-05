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
package fr.toutatice.ecm.platform.automation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author David Chevrier
 */
@Operation(id = FetchPublishingStatusChildren.ID, category = Constants.CAT_FETCH, label = "Fetch live tree with publishing infos",
        description = "Fetch children of document with publishing infos.")
public class FetchPublishingStatusChildren {

    public static final String ID = "Fetch.PublishingStatusChildren";

    private static final Log log = LogFactory.getLog(FetchPublishingStatusChildren.class);

    @Context
    protected CoreSession documentManager;

    @Param(name = "documentId", required = true)
    protected DocumentModel document;

    @Param(name = "liveStatus", required = true)
    protected boolean liveStatus;

    @OperationMethod
    public Object run() throws ClientException {
        JSONArray childrenWithStatus = new JSONArray();
        if (document.isProxy()) {
            log.warn("Document " + document.getId() + " is proxy: can't access children.");
            return new JSONArray();
        }
        DocumentModelList children = documentManager.getChildren(document.getRef());
        for (DocumentModel child : children) {
            if (liveStatus && !child.isProxy()) {
                // Live children
                JSONObject childWithStatus = new JSONObject();
                DocumentModel publishedChild = ToutaticeDocumentHelper.getProxy(documentManager, child, SecurityConstants.READ);
                boolean isPublished = publishedChild != null;
                childWithStatus.element("isPublished", isPublished);
                if (isPublished) {
                    boolean isLiveModifiedFromProxy = !child.getVersionLabel().equals(publishedChild.getVersionLabel());
                    childWithStatus.element("isLiveModifiedFromProxy", isLiveModifiedFromProxy);
                } else {
                    childWithStatus.element("isLiveModifiedFromProxy", false);
                }
                childWithStatus.element("docId", child.getId());
                childWithStatus.element("docPath", child.getPathAsString());
                childWithStatus.element("docType", child.getType());
                childWithStatus.element("docTitle", child.getTitle());
                boolean isFolderish = child.getFacets().contains("Folderish");
                childWithStatus.element("isFolderish", isFolderish);
                childrenWithStatus.add(childWithStatus);
            } else if (!liveStatus && child.isProxy()) {
                // Proxies children
                JSONObject childWithStatus = new JSONObject();
                childWithStatus.element("isPublished", true);
                DocumentModel srcDocument = documentManager.getSourceDocument(child.getRef());
                DocumentModel liveDocument = documentManager.getWorkingCopy(srcDocument.getRef());
                boolean isLiveModifiedFromProxy = !child.getVersionLabel().equals(liveDocument.getVersionLabel());
                childWithStatus.element("isLiveModifiedFromProxy", isLiveModifiedFromProxy);
                childWithStatus.element("docId", child.getId());
                childWithStatus.element("docPath", child.getPathAsString());
                childWithStatus.element("docType", child.getType());
                childWithStatus.element("docTitle", child.getTitle());
                boolean isFolderish = child.getFacets().contains("Folderish");
                childWithStatus.element("isFolderish", isFolderish);
                childrenWithStatus.add(childWithStatus);
            }
        }
        return new StringBlob(childrenWithStatus.toString(), "application/json");
    }

}
