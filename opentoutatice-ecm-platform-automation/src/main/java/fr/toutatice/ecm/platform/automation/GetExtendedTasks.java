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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.common.utils.i18n.I18NUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.JbpmService;


@Operation(id = GetExtendedTasks.ID, category = Constants.CAT_SERVICES, label = "Get extended tasks", since = "5.4",
        description = "List tasks assigned to this user or one of its group." + "Task properties are serialized using JSON and returned in a Blob.")
public class GetExtendedTasks // extends GetUserTasks
{

    public static final String ID = "Workflow.GetExtendedTasks";

    private static final Log log = LogFactory.getLog(Log.class);

    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession repo;

    @Context
    protected JbpmService srv;

    @OperationMethod
    public Blob run() throws Exception {
        NuxeoPrincipal principal = principal();
        List<TaskInstance> tasks = srv.getCurrentTaskInstances(principal, null);

        if (tasks == null) {
            return null;
        }

        JSONArray rows = new JSONArray();
        for (TaskInstance task : tasks) {
            DocumentModel doc = null;
            try {
                if (task.hasEnded() || task.isCancelled()) {
                    continue;
                }
                doc = srv.getDocumentModel(task, principal);
            } catch (Exception e) {
                log.warn("Cannot get doc for task " + task.getId() + ", error: " + e.getMessage());
            }
            if (doc == null) {
                log.warn(String.format("User '%s' has a task of type '%s' on an " + "unexisting or invisible document", principal.getName(), task.getName()));
            } else {
                if (!LifeCycleConstants.DELETED_STATE.equals(doc.getCurrentLifeCycleState())) {
                    JSONObject obj = new JSONObject();
                    // obj.element("id", task.getId());
                    obj.element("docid", doc.getId());
                    obj.element("doctype", doc.getType());
                    obj.element("docpath", doc.getPathAsString());
                    // obj.element("docname", URLEncoder.encode(doc.getPropertyValue("dc:title").toString(),"UTF-8"));
                    obj.element("docname", URLEncoder.encode(doc.getTitle(), "UTF-8"));

                    // String taskName = I18NUtils.getMessageString("messages", "label.workflow.task." + (String) task.getName(), null, Locale.FRENCH);
                    String taskName = I18NUtils.getMessageString("messages", (String) task.getName(), null, Locale.FRENCH);

                    obj.element("name", URLEncoder.encode(taskName, "UTF-8"));

                    // obj.element("description", task.getActorId()+task.getDescription()+task.getName());
                    /*
                     * String description = task.getDescription();
                     * if(description == null)
                     * description = "";
                     * obj.element("description", URLEncoder.encode(description,"UTF-8"));
                     */
                    obj.element("startDate", task.getCreate());
                    obj.element("dueDate", task.getDueDate());

                    // obj.element("directive", task.getVariableLocally(TaskVariableName.directive.name()));

                    /*
                     * @SuppressWarnings("unchecked")
                     * List<Comment> comments = task.getComments();
                     * String comment = "";
                     * if (comments != null && !comments.isEmpty()) {
                     * comment = comments.get(comments.size() - 1).getMessage();
                     * }
                     * if(StringUtils.isBlank(comment))
                     * {
                     * comment = "Pas de commentaire";
                     * }
                     * obj.element("comment", URLEncoder.encode(comment,"UTF-8"));
                     */

                    // task.getVariablesLocally() => documentRepositoryName=default, initiator=nxadam, documentId=0e233871-8943-4db6-8edb-0149a153e95f
                    // doc.getCurrentLifeCycleState() => "project"
                    // doc.getLifeCyclePolicy() => "default"
                    // TaskVariableName.directive.name() => directive
                    // task.getVariableLocally(TaskVariableName.directive.name()) => null
                    // task.getName() => "org.nuxeo.ecm.platform.publisher.jbpm.CoreProxyWithWorkflowFactory"

                    rows.add(obj);

                    /*
                     * {"id":1212416,"docid":"0e233871-8943-4db6-8edb-0149a153e95f","docname":"Hausse+du+chauffage+%C3%A0+De+Robien",
                     * "description":"nullnullorg.nuxeo.ecm.platform.publisher.jbpm.CoreProxyWithWorkflowFactory",
                     * "startDate":{"date":1,"day":3,"hours":16,"minutes":16,"month":11,"nanos":323000000,"seconds":55,"time":1291216615323,"timezoneOffset":-60,
                     * "year":110},"comment":""}
                     */
                }
            }
        }

        return (0 < rows.size()) ? new StringBlob(rows.toString(), "application/json") : null;
    }

    protected NuxeoPrincipal principal() {
        return (NuxeoPrincipal) ctx.getPrincipal();
    }

    protected JbpmListFilter filter() {
        return new JbpmListFilter() {

            private static final long serialVersionUID = 1L;

            @Override
            public <T> ArrayList<T> filter(JbpmContext jbpmContext, DocumentModel document, ArrayList<T> list, NuxeoPrincipal principal) {
                return list;
            }
        };
    }

}
