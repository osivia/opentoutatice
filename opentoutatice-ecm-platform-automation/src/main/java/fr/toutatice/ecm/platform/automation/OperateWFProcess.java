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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
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
import org.nuxeo.ecm.platform.jbpm.JbpmService;
import org.nuxeo.ecm.platform.jbpm.NuxeoJbpmException;
import org.nuxeo.ecm.platform.jbpm.TaskListFilter;
import org.nuxeo.ecm.platform.jbpm.VirtualTaskInstance;
import org.nuxeo.ecm.platform.jbpm.operations.GetRecipientsForTaskOperation;

@Operation(id = OperateWFProcess.ID, category = Constants.CAT_SERVICES, label = "Operate an action on the process", since = "5.3.2", description = "Permit to operate an action (validate, reject) on the process bound to the document.")
public class OperateWFProcess {
	public static final String ID = "Workflow.OperateProcess";

	//    private static final Log log = LogFactory.getLog(OperateWFProcess.class);
	
    @Context
    protected OperationContext ctx;

	@Context
	protected CoreSession coreSession;

	@Context
	protected transient JbpmService jbpmService;

	@Param(name = "task name", required = true, order = 0)
	protected String taskName;

	@Param(name = "transition", required = false, order = 1)
	protected String transition;

	@Param(name = "event", required = false, order = 2)
	protected String event;
	
	@Param(name = "comment", required = false, order = 3)
	protected String comment;


	protected ProcessInstance currentProcess;
	protected ArrayList<VirtualTaskInstance> currentVirtualTasks;

	@OperationMethod()
	public DocumentModel run(DocumentModel document) throws Exception {
		Principal principal = coreSession.getPrincipal();
		if (!(principal instanceof NuxeoPrincipal)) {
			throw new OperationException("Principal is not an instance of NuxeoPrincipal");
		}
		NuxeoPrincipal nuxpal = (NuxeoPrincipal) principal;

		TaskInstance taskInstance = getCurrentTasks(document, nuxpal, taskName);

		if (null != taskInstance) {
			Map<String, Serializable> taskVariables = new HashMap<String, Serializable>();
			taskVariables.put(JbpmService.TaskVariableName.validated.name(), Boolean.TRUE);
			jbpmService.endTask(Long.valueOf(taskInstance.getId()), 
					StringUtils.isBlank(transition) ? null : transition,
					taskVariables, 
					null, 
					getTransientVariables(document, nuxpal), 
					nuxpal);

			coreSession.save();

			if (StringUtils.isNotBlank(event)) {
	            Set<String> recipients = getRecipientsFromTask(taskInstance);
	            jbpmService.notifyEventListeners(event, 
	            		StringUtils.isBlank(comment) ? " " : comment, 
	            		recipients.toArray(new String[] {}), 
	            		coreSession, 
	            		nuxpal, 
	            		document);
	            
	            Events.instance().raiseEvent(event);
			}
		}
        
		return document;
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
	
	public TaskInstance getCurrentTasks(DocumentModel document, NuxeoPrincipal principal, String taskName)
	throws ClientException {
		TaskInstance currentTask = null;

		List<TaskInstance> currentTasks = new ArrayList<TaskInstance>();
		ProcessInstance currentProcess = getCurrentProcess(document, principal);
		if (currentProcess != null) {
			currentTasks.addAll(jbpmService.getTaskInstances(
					Long.valueOf(currentProcess.getId()), null,
					new TaskListFilter(taskName)));
		}

		if (0 < currentTasks.size()) {
			currentTask = currentTasks.get(currentTasks.size()-1);
		}

		return currentTask;
	}

    protected Map<String, Serializable> getTransientVariables(DocumentModel document, NuxeoPrincipal principal) {
        Map<String, Serializable> transientVariables = new HashMap<String, Serializable>();
        transientVariables.put(JbpmService.VariableName.document.name(), document);
        transientVariables.put(JbpmService.VariableName.principal.name(), principal);
        return transientVariables;
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getRecipientsFromTask(final TaskInstance taskInstance)
            throws NuxeoJbpmException {
        GetRecipientsForTaskOperation operation = new GetRecipientsForTaskOperation(
                taskInstance.getId());
        return (Set<String>) jbpmService.executeJbpmOperation(operation);

    }
}
