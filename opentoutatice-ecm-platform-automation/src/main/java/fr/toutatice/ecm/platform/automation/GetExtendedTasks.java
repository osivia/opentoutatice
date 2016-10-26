/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/) and others.
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
 */
package fr.toutatice.ecm.platform.automation;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskService;

@Operation(id = GetExtendedTasks.ID, category = Constants.CAT_SERVICES, label = "Get extended tasks", since = "5.4", description = "List tasks assigned to this user or one of its group."
		+ "Task properties are serialized using JSON and returned in a Blob.")
public class GetExtendedTasks // extends GetUserTasks
{

	public static final String ID = "Workflow.GetExtendedTasks";

	private static final Log log = LogFactory.getLog(Log.class);

	@Context
	protected OperationContext ctx;

	@Context
	protected CoreSession repo;

	@Context
	protected TaskService taskService;

	@Param(name = "wkflsNames",required = false)
	protected StringList wkfls;

	@OperationMethod
	public Blob run() throws Exception {
		NuxeoPrincipal principal = principal();

		List<Task> tasks = taskService.getCurrentTaskInstances(repo);

		if (tasks == null) {
			return null;
		}

		JSONArray rows = new JSONArray();
		boolean exposeTask = true;
		for (Task task : tasks) {
			DocumentModel doc = null;
		
			if(this.wkfls!=null && !this.wkfls.isEmpty()){
			 String wkflName = getProcessName(task);
			 exposeTask = this.wkfls.contains(wkflName);
			} 
			
			if (exposeTask) {
				try {
					if (task.hasEnded() || task.isCancelled()) {
						continue;
					}
					doc = taskService.getTargetDocumentModel(task, repo);
				} catch (Exception e) {
					log.warn("Cannot get doc for task " + task.getId() + ", error: " + e.getMessage());
				}
				if (doc == null) {
					log.warn(String.format(
							"User '%s' has a task of type '%s' on an " + "unexisting or invisible document",
							principal.getName(), task.getName()));
				} else {
					if (!LifeCycleConstants.DELETED_STATE.equals(doc.getCurrentLifeCycleState())) {
						JSONObject obj = new JSONObject();
						obj.element("docid", doc.getId());
						obj.element("doctype", doc.getType());
						obj.element("docpath", doc.getPathAsString());

						obj.element("docname", URLEncoder.encode(doc.getTitle(), "UTF-8"));

						String taskName = I18NUtils.getMessageString("messages", (String) task.getName(), null,
								Locale.FRENCH);

						obj.element("name", URLEncoder.encode(taskName, "UTF-8"));

						obj.element("startDate", task.getCreated());
						obj.element("dueDate", task.getDueDate());

						rows.add(obj);
						
					}
				}
			}
		}

		return (0 < rows.size()) ? new StringBlob(rows.toString(), "application/json") : null;
	}

	private String getProcessName(Task task) {
		final String processId = task.getProcessId();
		
		DocumentModel processDoc = repo.getDocument(new IdRef(processId));
		String name = processDoc.getName();
		if(name.contains(".")){
			name = name.substring(0, name.indexOf('.'));
		}		
		return name;
	}

	protected NuxeoPrincipal principal() {
		return (NuxeoPrincipal) ctx.getPrincipal();
	}

}
