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

        }

        /* To force ES re-indexing */
        Map<String, Serializable> properties = new HashMap<String, Serializable>(1);
        properties.put("checkedInVersionRef", newProxy.getRef());// FIXME: to avoid nullPointer in AnnotationFulltextEventListener...
        ToutaticeNotifyEventHelper.notifyEvent(super.coreSession, DocumentEventTypes.DOCUMENT_CHECKEDIN, newProxy, new HashMap<String, Serializable>(0));

        return newPulishedDoc;
    }


}
