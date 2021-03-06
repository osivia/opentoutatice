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

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.web.RoutingTaskActionsBean;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskImpl;
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
	private static final List<String> TOUTATICE_WF_ONLINE_ACTIONS = new ArrayList<String>() {
		private static final long serialVersionUID = 3710687789887853309L;
		
		{
			add(ToutaticeGlobalConst.CST_WORKFLOW_BUTTON_ONLINE_ACCEPT);
			add(ToutaticeGlobalConst.CST_WORKFLOW_BUTTON_ONLINE_REJECT);
		}
	};

	@In
	TaskService taskService;

	@In(create = true)
	ToutaticeDocumentRoutingActionsBean routingActions;
	
	public String getClickedButton() {
		return super.button;
	}

	public String getWorkFlowInitiator() throws ClientException {
		return routingActions.getCurrentWorkflowInitiator();
	}

	@Override
    public String endTask(Task task) throws ClientException {
		String view = MainTabsActions.DEFAULT_VIEW;
		
		if (TOUTATICE_WF_ONLINE_ACTIONS.contains(getClickedButton())) {
			DocumentModel currentDoc = navigationContext.getCurrentDocument();
			Task taskForNotif = new TaskImpl(task.getDocument());
			String wfInitiator = getWorkFlowInitiator();
			
			String eventName;
			if(ToutaticeGlobalConst.CST_WORKFLOW_BUTTON_ONLINE_ACCEPT.equalsIgnoreCase(getClickedButton())){
				eventName = ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_APPROVED;
			} else {
				eventName = ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_REJECTED;
			}
			
			super.endTask(task);
			
			ToutaticeWorkflowHelper.notifyRecipients(documentManager, taskForNotif, currentDoc, wfInitiator, eventName);
		} else {
			//no-op. only forward processing to mother class
			view = super.endTask(task);
		}
        
        return view;
    }
	
}
