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
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.lang.ArrayUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeOperationHelper;
import fr.toutatice.ecm.platform.core.utils.exception.ToutaticeException;


/**
 * @author David Chevrier
 */
public class ToutaticeAsynchronousUpdateListener implements PostCommitEventListener {

    private static final String UPDATE_DOMAIN_CHAIN = "updateDomain";
    private static final String MOVE_OP_CHAIN = "moveOp";

	private static final String DOCUMENT_MODIFIED = "documentModified";

	private static final String[] SELECTED_EVENTS = {"documentCreated", "documentCreatedByCopy", "documentMoved", "documentRestored"};

	@Override
	public void handleEvent(EventBundle events) throws ClientException {
		for (Event event : events) {

			if (event.getContext() instanceof DocumentEventContext) {
				EventContext ctx = event.getContext();
				DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
				DocumentModel document = docCtx.getSourceDocument();
				CoreSession session = ctx.getCoreSession();

				if (document.isImmutable()) {
					// ignore immutable documents
					return;
				}
				
				try {                       
					if (DOCUMENT_MODIFIED.equals(event.getName()) && ToutaticeNuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(document.getType())) {
							ToutaticeOperationHelper.runOperationChain(session, UPDATE_DOMAIN_CHAIN, document);						
					} else if (ArrayUtils.contains(SELECTED_EVENTS, event.getName())) {						
							ToutaticeOperationHelper.runOperationChain(session, MOVE_OP_CHAIN, document);						
					}
				} catch (ToutaticeException e) {
					throw new ClientException(e);
				}
			}
		}
	}

}
