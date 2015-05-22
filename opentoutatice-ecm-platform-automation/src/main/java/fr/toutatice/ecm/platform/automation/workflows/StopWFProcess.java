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
package fr.toutatice.ecm.platform.automation.workflows;

import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.core.api.DocumentRoutingEngineService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskImpl;

import fr.toutatice.ecm.platform.automation.exceptions.WorkflowManagmentException;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;


/**
 * @author David Chevrier
 */
@Operation(id = StopWFProcess.ID, category = Constants.CAT_SERVICES, label = "Stop process", since = "1.2.2",
        description = "Stop (or cancel) a process bound to the document.")
public class StopWFProcess {

    public static final String ID = "Workflow.CancelProcess";

    @Context
    protected CoreSession documentManager;
    
    @Context
    protected OperationContext context;
    
    @Context 
    protected transient AutomationService automationService;

    @Context
    protected transient DocumentRoutingEngineService engineRoutingService;

    @Param(name = "workflow name", required = true, order = 0)
    protected String inputWorkflowName;

    @OperationMethod()
    public void run(DocumentModel document) throws Exception {

        DocumentRoute inputWorkflowRoute = ToutaticeWorkflowHelper.getWorkflowByName(inputWorkflowName, document);
        if (inputWorkflowRoute != null) {

            Task currentTask = getCurrentTask(inputWorkflowName, documentManager, document);

            engineRoutingService.cancel(inputWorkflowRoute, documentManager);
            
            if(currentTask != null){
                ToutaticeWorkflowHelper.notifyRecipients(documentManager, currentTask, document, null, ToutaticeGlobalConst.CST_EVENT_ONLINE_WF_CANCELED);
            } else {
                throw new WorkflowManagmentException("Workflow " + inputWorkflowName + " has no current task");
            }

        } else {
            throw new WorkflowManagmentException("There is no " + inputWorkflowName + " workflow instance to cancel");
        }

    }

    /* Temporary: we must be generic: task as parameter? or ... no need for task? */
    private Task getCurrentTask(String wfName, CoreSession session, DocumentModel document) {
        Task currentTask = null;
        
        if (ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE.equals(wfName)) {

            Task task = ToutaticeWorkflowHelper.getTaskByName(ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE, session, document);
            currentTask = new TaskImpl(task.getDocument());
        }
        
        return currentTask;
    }

}
