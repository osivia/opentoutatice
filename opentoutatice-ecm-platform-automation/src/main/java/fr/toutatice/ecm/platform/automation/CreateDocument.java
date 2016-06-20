/*
 * (C) Copyright 2016 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 *   kle-helley
 */
package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.automation.core.util.Properties;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.pathsegment.PathSegmentService;

@Operation(
		id = CreateDocument.ID,
		category = Constants.CAT_DOCUMENT,
		label = "Create a new document",
		description = "Create a new document in the input folder. If the 'name' parameter is not set, a new name will be derived from the document title, using the same "
				+ "naming strategy as Nuxeo when using the GUI (if the document has a title). This is the only difference between this operation and 'Document.Create', "
				+ "with the latter defaulting the name to 'Untitled'.")
public class CreateDocument {

	public static final String ID = "Document.TTCCreate";
	private static final String PROP_TITLE = "dc:title";

	@Context
	protected CoreSession session;

	@Context
	protected PathSegmentService pathSegmentService;

	@Param(name = "type")
	protected String type;

	@Param(name = "name", required = false)
	protected String name;

	@Param(name = "properties", required = false)
	protected Properties content;

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(final DocumentModel doc) throws Exception {
		if (name == null) {
			if ((content != null) && (content.get(PROP_TITLE) != null)) {
				name = pathSegmentService.generatePathSegment(content.get(PROP_TITLE));
			} else {
				name = "Untitled";
			}
		}

		final DocumentModel newDoc = session.createDocumentModel(doc.getPathAsString(), name, type);

		if (content != null) {
			DocumentHelper.setProperties(session, newDoc, content);
		}
		return session.createDocument(newDoc);
	}

	@OperationMethod(collector = DocumentModelCollector.class)
	public DocumentModel run(final DocumentRef doc) throws Exception {
		return run(session.getDocument(doc));
	}

}
