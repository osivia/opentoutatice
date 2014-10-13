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

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.routing.api.exception.DocumentRouteException;
import org.nuxeo.ecm.platform.routing.core.impl.GraphNode;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskImpl;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

@Operation(id = OperateWFProcess.ID, category = Constants.CAT_SERVICES, label = "Operate an action on the process", since = "5.3.2", description = "Permit to operate an action (validate, reject) on the process bound to the document.")
public class OperateWFProcess {
    
	public static final String ID = "Workflow.OperateProcess";

	@Context
	protected CoreSession coreSession;
	
	@Context
	protected transient DocumentRoutingService routingService;
	
	@Context
    protected transient TaskService taskService;

	@Param(name = "task name", required = true, order = 0)
	protected String taskName;

	@Param(name = "transition", required = false, order = 1)
	protected String transition;
	
	@Param(name = "comment", required = false, order = 3)
	protected String comment;

	@OperationMethod()
	public DocumentModel run(DocumentModel document) throws Exception {
		
		Task task = ToutaticeWorkflowHelper.getDocumentTaskByName(taskName, coreSession, document);
		Task taskForNotif = new TaskImpl(task.getDocument());
		
		String initiator = ToutaticeWorkflowHelper.getOnLineWorkflowInitiator(document);
		if(StringUtils.isNotBlank(comment)){
		    Principal principal = coreSession.getPrincipal();
		    task.addComment(principal.getName(), comment);
		}
		
		//FIXME: no matter if formVariable = new HashMap<String, Object>(0)?
		routingService.endTask(coreSession, task, new HashMap<String, Object>(0), transition);
		
		ToutaticeWorkflowHelper.notifyRecipients(coreSession, taskForNotif,
		        document, initiator,
                getEvent(transition));
		
		return document;

	}

    protected String getEvent(String transition) {
        String event = StringUtils.EMPTY;
        if (ToutaticeGlobalConst.CST_WORKFLOW_ONLINE_ACCEPT_TRANSITION.equals(transition)) {
            event = ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_APPROVED;
        } else {
            event = ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_REJECTED;
        }
        return event;
    }
	
}
