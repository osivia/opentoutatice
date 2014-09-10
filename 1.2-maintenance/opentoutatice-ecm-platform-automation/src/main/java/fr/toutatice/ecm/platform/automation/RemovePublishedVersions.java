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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

@Operation(id = RemovePublishedVersions.ID, category = Constants.CAT_DOCUMENT, label = "Remove all published versions of a document", description = "Remove all published versions of a document among its versions series and inside a specific target folder/section. Return the input document.")
public class RemovePublishedVersions {

	public static final String ID = "Document.RemovePublishedVersions";

	@Context
	protected CoreSession session;

	@Param(name = "target", required = true)
	protected DocumentModel target;

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentModel document) throws Exception {
				
		UnrestrictedRemovePublishedVersionsRunner removeRunner = new UnrestrictedRemovePublishedVersionsRunner(session, document, target);	
		removeRunner.runUnrestricted();
		return removeRunner.getReturnedDocument();
	}
	
	private static class UnrestrictedRemovePublishedVersionsRunner extends UnrestrictedSessionRunner {
		
		private DocumentModel document;
		private DocumentModel target;
		private DocumentModel returnedDocument;
		
		public DocumentModel getReturnedDocument() {
			return returnedDocument;
		}

		public UnrestrictedRemovePublishedVersionsRunner(CoreSession session, DocumentModel document, DocumentModel target) {
			super(session);
			this.document = document;
			this.target = target;
		}

		@Override
		public void run() throws ClientException {
			DocumentRef targetRef = this.target.getRef();
			DocumentRef baseDocRef = document.getRef();
			returnedDocument = this.document;

			if (this.document.isVersion()) {
				String sourceDocId = this.document.getSourceId();
				baseDocRef = new IdRef(sourceDocId);
			}
			
			/* gérer le cas où le document à retirer est un document proxy.
			 * Comme le document sera supprimé, il faut retourner sur le document
			 * parent (navigation).
			*/
			if (this.document.isProxy()) {
				returnedDocument = this.target;
			}
			
			if (null != targetRef) {
				DocumentModelList proxies = this.session.getProxies(baseDocRef, targetRef);
				for (DocumentModel proxy : proxies) {
					this.session.removeDocument(proxy.getRef());
				}
			} else {
				throw new ClientException("Failed to get the target document reference");
			}
		}

	}

}
