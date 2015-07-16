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
package fr.toutatice.ecm.platform.core.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Faire en sorte que les documents modifiés en mode dit "silencieux" soient bien indexés par ElasticSearch.
 * (http://redmine.toutatice.fr/issues/3802)
 */
public class ToutaticeElasticSearchInlineListener implements EventListener {
	
	public static final Log log = LogFactory.getLog(ToutaticeElasticSearchInlineListener.class);
	public static final String ELASTICSEARCH_LISTENER_ID = "elasticSearchInlineListener";

	private EventService eventService;

	@Override
	public void handleEvent(Event event) throws ClientException {
		if (event.getContext() instanceof DocumentEventContext) {
			DocumentEventContext ctx = (DocumentEventContext) event.getContext();
			DocumentModel document = ctx.getSourceDocument();
			String originalEvtName = (String) ctx.getProperty(ToutaticeGlobalConst.CST_EVENT_OPTION_KEY_ORIGINAL_EVENT_NAME);
			
			EventListenerDescriptor listenerDesc = getEventService().getEventListener(ELASTICSEARCH_LISTENER_ID);
			if (null != document && null != listenerDesc && listenerDesc.isEnabled() && listenerDesc.acceptEvent(originalEvtName)) {
				EventImpl docEvt = new EventImpl(originalEvtName, event.getContext());
				listenerDesc.asEventListener().handleEvent(docEvt);
				
				log.debug("ElasticSearch: re-indexed the document '" + document.getPathAsString() + "'");
				
				// forcer la ré-indexation du proxy du document courant si ce dernier est une version publiée localement
				if (document.isVersion()) {
					DocumentModel proxy = ToutaticeDocumentHelper.getProxy(ctx.getCoreSession(), document, null);
					if (null != proxy) {
						EventContext proxyCtx = new DocumentEventContext(ctx.getCoreSession(), ctx.getPrincipal(), proxy);
						EventImpl proxyEvt = new EventImpl(originalEvtName, proxyCtx);
						listenerDesc.asEventListener().handleEvent(proxyEvt);
						log.debug("ElasticSearch: re-indexed the document's proxy'" + proxy.getPathAsString() + "'");
					}
				}
			}
		}
	}
	
	private EventService getEventService() throws ClientException {
		if (null == this.eventService) {
			this.eventService = Framework.getService(EventService.class);
		}
		
		if (null == this.eventService) {
			throw new ClientException("failed to instanciate the 'EventService'");
		}
		
		return this.eventService;
	}
	
}
