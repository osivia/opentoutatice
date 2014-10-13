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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.task.Task;

import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

@Operation(id = SendTaskNotification.ID, category = Constants.CAT_NOTIFICATION, label = "Send a notification event", since = "5.3.2", description = "Send a notification that aims to be catched and processed by the notification event service & listener. The event must be declared via the contribution to the target 'org.nuxeo.ecm.platform.ec.notification.service.NotificationService', point 'notifications'. The input is returned") 
public class SendTaskNotification {
	public static final String ID = "Notification.SendTaskNotification";

//	private static final Log log = LogFactory.getLog(SendTaskNotification.class);

    @Context
    protected OperationContext ctx;

	@Context
	protected CoreSession coreSession;
	
	@Param(name = "task name", required = true, order = 0)
	protected String taskName;

	@Param(name = "event", required = true, order = 1)
	protected String event;
	
	@Param(name = "actors", required = false, order = 2)
    protected String keyForActors;
	
	@Param(name = "comment", required = false, order = 3)
	protected String comment;
	
	@OperationMethod()
	@SuppressWarnings("unchecked")
	public DocumentModel run(DocumentModel document) throws Exception {
	    Principal principal = getPrincipal();
	    
	    Task task = ToutaticeWorkflowHelper.getDocumentTaskByName(taskName, coreSession, document);
	    if(StringUtils.isNotBlank(keyForActors)){
	        task.setActors(getGivenActors(principal));
	    }
	    if(StringUtils.isNotBlank(comment)){
	        task.addComment(principal.getName(), comment);
	    }
	    
	    ToutaticeWorkflowHelper.notifyRecipients(coreSession, task, document, task.getInitiator(), event);
        
		return document;
	}

    protected List<String> getGivenActors(Principal principal) throws OperationException {
        NuxeoPrincipal nuxpal = (NuxeoPrincipal) principal;
        
        List<String> recipients = new ArrayList<String>();
        Object actors = ctx.get(keyForActors);
        
        if (actors != null) {
            boolean throwError = false;
            try {
                if (actors instanceof List) {
                    recipients.addAll((List<String>) actors);
                } else if (actors instanceof String[]) {
                    for (String actor : (String[]) actors) {
                        recipients.add(actor);
                    }
                } else if (actors instanceof String) {
                    recipients.add((String) actors);
                } else {
                    throwError = true;
                }
            } catch (ClassCastException e) {
                throwError = true;
            }
            
            if (throwError) {
                throw new OperationException(String.format("Invalid key to retrieve a list, array or single "
                        + "string of prefixed actor "
                        + "ids '%s', value is not correct: %s",
                        keyForActors, actors));
            }
        }
        return recipients;
    }
    
    protected Principal getPrincipal() throws OperationException {
        Principal principal = coreSession.getPrincipal();
        if (!(principal instanceof NuxeoPrincipal)) {
            throw new OperationException("Principal is not an instance of NuxeoPrincipal");
        }
        return principal;
    }
	
}
