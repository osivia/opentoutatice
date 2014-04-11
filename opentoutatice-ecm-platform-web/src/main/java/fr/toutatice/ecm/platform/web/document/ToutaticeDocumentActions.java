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
package fr.toutatice.ecm.platform.web.document;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

public interface ToutaticeDocumentActions extends DocumentActions {

	 public List<String> getDocumentPathSegments(DocumentModel document, DocumentModel referenceDoc);
	 public boolean hasProxy(DocumentModel document) throws ClientException;
	 public DocumentModel getProxy(DocumentModel document) throws ClientException;
	 public boolean belongToPublishSpace();
	 public boolean belongToWorkSpace();
	 public String getDocumentPermalink() throws ClientException;
	 public boolean hasChildrenWithType(String type) throws ClientException;
	 public void updateDocWithMapSwitch(DocumentModel document) throws PropertyException, ClientException;
	 public String updateNUpgradeCurrentDocument(String version) throws ClientException;
	 public String updateDocument(DocumentModel doc) throws ClientException;
	 public String getProxyVersion(DocumentModel document) throws ClientException;
}
