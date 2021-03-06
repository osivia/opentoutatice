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
package fr.toutatice.ecm.platform.web.publication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeCommentsHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

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

			if(!newProxy.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)){
				newProxy.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY);
			}

            // User has not necessary Write permission on proxy
            ToutaticeDocumentHelper.saveDocumentSilently(super.coreSession, newProxy, true);

		} else {
			newPulishedDoc = super.publishDocument(doc, targetNode, params); 
			newProxy = ((SimpleCorePublishedDocument) newPulishedDoc).getProxy();

			if(!newProxy.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)){
				newProxy.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY);

                // User has not necessary Write permission on proxy
                ToutaticeDocumentHelper.saveDocumentSilently(super.coreSession, newProxy, true);
			}

		}

		return newPulishedDoc;
	}
}
