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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.core.Events;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
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
import org.nuxeo.ecm.platform.jbpm.JbpmEventNames;
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.TaskListFilter;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;

@Operation(id = StartWFProcess.ID, category = Constants.CAT_SERVICES, label = "Create process", since = "5.3.2", description = "Enable to create a process bound to the document.")
public class StartWFProcess {
	public static final String ID = "Workflow.CreateProcess";

	//    private static final Log log = LogFactory.getLog(StartWFProcess.class);
	
    @Context
    protected OperationContext ctx;

	@Context
	protected CoreSession coreSession;

	@Context
	protected transient JbpmService jbpmService;

	@Param(name = "workflow type", required = true, order = 0)
	protected String pd;

	@Param(name = "task name", required = true, order = 1)
	protected String td;

	@Param(name = "following state", required = true, order = 2)
	protected String endLifeCycle;

    @Param(name = "actors", required = false, order = 3)
    protected String keyForActors;

    @Param(name = "due date", required = false, order = 4)
    protected Date dueDate;

    @Param(name = "directive", required = false, order = 5)
    protected String directive;

    @Param(name = "comment", required = false, order = 6)
    protected String comment;

	@Param(name = "right", required = true, order = 7)
	protected String right;

	protected ProcessInstance currentProcess;
	protected ArrayList<VirtualTaskInstance> currentVirtualTasks;

	@OperationMethod()
	@SuppressWarnings("unchecked")
	public DocumentModel run(DocumentModel document) throws Exception {

		Principal principal = coreSession.getPrincipal();
		if (!(principal instanceof NuxeoPrincipal)) {
			throw new OperationException("Principal is not an instance of NuxeoPrincipal");
		}
		NuxeoPrincipal nuxpal = (NuxeoPrincipal) principal;

		Map<String, Serializable> map = null;
		if (endLifeCycle != null && !endLifeCycle.equals("") && !"null".equals(endLifeCycle)) {
			map = new HashMap<String, Serializable>();
			map.put(JbpmService.VariableName.endLifecycleTransition.name(), endLifeCycle);
		}

		jbpmService.createProcessInstance(nuxpal, pd, document, map, null);

		// register the participants onto the process
		ProcessInstance pi = getCurrentProcess(document, nuxpal);
        List<VirtualTaskInstance> virtualTasks = getCurrentVirtualTasks(document, nuxpal);
        if (virtualTasks == null) {
            virtualTasks = new ArrayList<VirtualTaskInstance>();
        }
        
        List<String> prefixedActorIds = new ArrayList<String>();
        Object actors = ctx.get(keyForActors);
        if (actors != null) {
        	boolean throwError = false;
        	try {
        		if (actors instanceof List) {
        			prefixedActorIds.addAll((List<String>) actors);
        		} else if (actors instanceof String[]) {
        			for (String actor : (String[]) actors) {
        				prefixedActorIds.add(actor);
        			}
        		} else if (actors instanceof String) {
        			prefixedActorIds.add((String) actors);
        		} else {
        			throwError = true;
        		}
        	} catch (ClassCastException e) {
        		throwError = true;
        	}
            if (throwError) {
                throw new OperationException(String.format(
                        "Invalid key to retrieve a list, array or single "
                                + "string of prefixed actor "
                                + "ids '%s', value is not correct: %s",
                        keyForActors, actors));
            }
        }

        VirtualTaskInstance newVirtualTask = new VirtualTaskInstance();
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("right", right);
        
        newVirtualTask.setActors(prefixedActorIds);
        newVirtualTask.setDirective(directive);
        newVirtualTask.setDueDate(dueDate);
        newVirtualTask.setComment(comment);
        newVirtualTask.setParameters(parameters);
        
        virtualTasks.add(newVirtualTask);
        
        pi.getContextInstance().setVariable(JbpmService.VariableName.participants.name(), virtualTasks);
        jbpmService.persistProcessInstance(pi);
		
		// close the first process step: choose participants. Waiting for task completion now.
		TaskInstance startTask = getStartTask(document, nuxpal, td);
		if (startTask.hasEnded()) {
			throw new ClientException("Process is already started");
		}

		Map<String, Serializable> transientVariables = new HashMap<String, Serializable>();
		transientVariables.put(JbpmService.VariableName.participants.name(), getCurrentVirtualTasks(document, nuxpal));
		transientVariables.put(JbpmService.VariableName.document.name(), document);
		transientVariables.put(JbpmService.VariableName.principal.name(), nuxpal);
		jbpmService.endTask(Long.valueOf(startTask.getId()), null, null, null, transientVariables, nuxpal);
		coreSession.save();
		
        jbpmService.notifyEventListeners(JbpmEventNames.WORKFLOW_NEW_STARTED, 
        		"", 
        		new String[] {NuxeoPrincipal.PREFIX + nuxpal.getName()}, 
        		coreSession, 
        		nuxpal, 
        		document);
        
        Events.instance().raiseEvent(JbpmEventNames.WORKFLOW_NEW_STARTED);
        
		return document;
	}

	protected TaskInstance getStartTask(DocumentModel document, NuxeoPrincipal principal, String taskName) throws ClientException {
		TaskInstance startTask = null;
		if (taskName != null) {
			// get task with that name on current process
			ProcessInstance pi = getCurrentProcess(document, principal);
			if (pi != null) {
				List<TaskInstance> tasks = jbpmService.getTaskInstances(
						Long.valueOf(currentProcess.getId()), null,
						new TaskListFilter(taskName));
				if (tasks != null && !tasks.isEmpty()) {
					// take first one found
					startTask = tasks.get(0);
				}
			}
		}
		if (startTask == null) {
			throw new ClientException(
					"No start task found on current process with name "
					+ taskName);
		}
		return startTask;
	}

	public ProcessInstance getCurrentProcess(DocumentModel document, NuxeoPrincipal principal) throws ClientException {
		if (currentProcess == null) {
			List<ProcessInstance> processes = jbpmService.getProcessInstances(document, principal, null);
			if (processes != null && !processes.isEmpty()) {
				currentProcess = processes.get(0);
			}
		}
		return currentProcess;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<VirtualTaskInstance> getCurrentVirtualTasks(DocumentModel document, NuxeoPrincipal principal)
	throws ClientException {
		if (currentVirtualTasks == null) {
			currentVirtualTasks = new ArrayList<VirtualTaskInstance>();
			ProcessInstance currentProcess = getCurrentProcess(document, principal);
			if (currentProcess != null) {
				Object participants = currentProcess.getContextInstance().getVariable(
						JbpmService.VariableName.participants.name());
				if (participants != null && participants instanceof List) {
					currentVirtualTasks.addAll((List<VirtualTaskInstance>) participants);
				}
			}
		}
		return currentVirtualTasks;
	}
}
