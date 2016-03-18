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
 *   dchevrier
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.core.listener;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeQueryHelper;

public class ToutaticeDocumentMoved implements EventListener {

	@Override
	public void handleEvent(Event event) throws ClientException {
		if (event.getContext() instanceof DocumentEventContext) {
			DocumentEventContext ctx = (DocumentEventContext) event.getContext();
			DocumentModel movedDocument = ctx.getSourceDocument();
			CoreSession session = ctx.getCoreSession();
			
			// filtrer les documents de type proxy pour éviter les boucles de traitement
			if (!movedDocument.isProxy()) {
				UnrestrictedSessionRunner runner = new UnrestrictedMoveProxyRunner(session, ctx, movedDocument);
				runner.runUnrestricted();
			}
		}
	}
	
	private class UnrestrictedMoveProxyRunner extends UnrestrictedSessionRunner {
		private EventContext ctx;
		private DocumentModel movedDocument;

		public UnrestrictedMoveProxyRunner(CoreSession session, EventContext ctx, DocumentModel document) {
			super(session);
			this.ctx = ctx;
			this.movedDocument = document;
		}
		
		@Override
		public void run() throws ClientException {
			// vérifier qu'il ne s'agit pas d'un renommage de document
			DocumentRef srcFolderRef = (DocumentRef) this.ctx.getProperty(CoreEventConstants.PARENT_PATH);
			DocumentRef dstFolderRef = this.movedDocument.getParentRef();
			if (!srcFolderRef.equals(dstFolderRef)) {
				
				// vérifier si le document déplacé était publié (possédait un proxy)
				DocumentModelList proxies = this.session.getProxies(this.movedDocument.getRef(), srcFolderRef);

				// déplacer les proxies (et juxtaposition avec la cible) 
				if (null != proxies && 0 < proxies.size()) {
					for (DocumentModel proxy : proxies) {
					    this.session.move(proxy.getRef(), dstFolderRef, null);
					    // To commit proxy move before reordoring it
					    this.session.save();
						this.session.orderBefore(dstFolderRef, proxy.getName(), this.movedDocument.getName());
					}
				}
			}
		}
		
	}
	
}
