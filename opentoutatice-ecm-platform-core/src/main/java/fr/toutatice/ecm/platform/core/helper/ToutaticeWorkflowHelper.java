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
package fr.toutatice.ecm.platform.core.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ec.notification.NotificationConstants;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.core.service.TaskEventNotificationHelper;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;

/**
 * @author David Chevrier
 * 
 */
public final class ToutaticeWorkflowHelper {
	
	private ToutaticeWorkflowHelper() {
	}

	public static void notifyRecipients(CoreSession documentManager, Task task,
			DocumentModel document, String initiator, String event)
			throws ClientException {
		NuxeoPrincipal principal = (NuxeoPrincipal) documentManager
				.getPrincipal();

		Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();
		ArrayList<String> notificationRecipients = new ArrayList<String>();
		if (initiator != null) {
			notificationRecipients.add(initiator);
		}
		notificationRecipients.addAll(task.getActors());
		eventProperties.put(NotificationConstants.RECIPIENTS_KEY,
				notificationRecipients
						.toArray(new String[notificationRecipients.size()]));

		TaskEventNotificationHelper.notifyEvent(documentManager, document,
				principal, task, event, eventProperties, null, null);
	}

	public static DocumentRoute getOnLineWorkflow(DocumentModel currentDoc) {
		DocumentRoute onLineWF = null;
		if (currentDoc != null) {
			DocumentRoutingService routingService = Framework
					.getLocalService(DocumentRoutingService.class);
			CoreSession coreSession = currentDoc.getCoreSession();
			List<DocumentRoute> documentRoutes = routingService
					.getDocumentRoutesForAttachedDocument(coreSession,
							currentDoc.getId());
			if (documentRoutes != null && !documentRoutes.isEmpty()) {
				int index = 0;
				while (index < documentRoutes.size() && onLineWF == null) {
					DocumentRoute route = documentRoutes.get(index);
					if (ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE
							.equalsIgnoreCase(route.getName())) {
						onLineWF = route;
					}
					index++;
				}
			}
		}
		return onLineWF;
	}
	
	public static boolean isOnLineWorkflow(Path docPath) {
		 String[] segments = docPath.segments();
		 for(String segment : segments){
			 if(StringUtils.contains(segment, ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE)){
				 return true;
			 }
		 }
		 return false;
	}

	public static boolean isOnLineWorkflow(DocumentModel currentDoc) {
		return getOnLineWorkflow(currentDoc) != null;
	}

	public static String getOnLineWorkflowInitiator(DocumentModel currentDoc)
			throws PropertyException, ClientException {
		String initiator = StringUtils.EMPTY;
		DocumentRoute route = getOnLineWorkflow(currentDoc);
		if (route != null) {
			initiator = (String) route.getDocument().getPropertyValue(
					DocumentRoutingConstants.INITIATOR);
		}
		return initiator;
	}

}
