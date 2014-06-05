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
package fr.toutatice.ecm.platform.web.workflows;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.web.RoutingTaskActionsBean;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;
import org.nuxeo.ecm.webapp.action.MainTabsActions;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

/**
 * @author David Chevrier
 * 
 */
@Scope(CONVERSATION)
@Name("routingTaskActions")
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeRoutingTaskActionsBean extends RoutingTaskActionsBean {

	private static final long serialVersionUID = -5854741772106895168L;
	
	@In
	TaskService taskService;
	
	@In(create = true)
	ToutaticeDocumentRoutingActionsBean routingActions;

	public boolean isAcceptOnLineButtonCliked() {
		return ToutaticeGlobalConst.CST_WORKFLOW_BUTTON_ONLINE_ACCEPT
				.equalsIgnoreCase(super.button);
	}

	public String getWorkFlowInitiator() throws ClientException {
		return routingActions.getCurrentWorkflowInitiator();
	}

	@Override
	public String endTask(Task task) throws ClientException {
		DocumentModel currentDoc = navigationContext.getCurrentDocument();
		String wfInitiator = getWorkFlowInitiator();
		if (isAcceptOnLineButtonCliked()) {
			ToutaticeWorkflowHelper.notifyRecipients(documentManager, task,
					currentDoc, wfInitiator, 
					ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_APPROVED);
		} else {
			ToutaticeWorkflowHelper.notifyRecipients(documentManager, task,
					currentDoc, wfInitiator, 
					ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_REJECTED);
		}
		// Done at end to keep attributes for code above
		super.endTask(task);
		
		return MainTabsActions.DEFAULT_VIEW;
	}

}
