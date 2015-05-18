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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.WorkManagerImpl;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

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
						
						if(defaultWorksEnded()){
							ToutaticeOperationHelper.runOperationChain(session, UPDATE_DOMAIN_CHAIN, document);
						}
						
					} else if (ArrayUtils.contains(SELECTED_EVENTS, event.getName())) {
						
						if(defaultWorksEnded()){
							ToutaticeOperationHelper.runOperationChain(session, MOVE_OP_CHAIN, document);
						}
						
					}
				} catch (ToutaticeException e) {
					throw new ClientException(e);
				}
			}
		}
	}
	
	/**
	 * @return true if all works of default queue are ended.
	 * @throws ToutaticeException 
	 * @throws Exception 
	 */
	protected boolean defaultWorksEnded() throws ToutaticeException {
		boolean ended = false;
		
		try {
			WorkManager mngWk = (WorkManager) Framework.getService(WorkManager.class);
			ended = mngWk.awaitCompletion(WorkManagerImpl.DEFAULT_QUEUE_ID, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			throw new ToutaticeException(e);
		}
		
		return ended;
	}

}
