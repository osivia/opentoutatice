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
 */
package fr.toutatice.ecm.platform.web.workflows;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.web.TaskActionsBean;
import org.nuxeo.ecm.platform.ui.web.invalidations.AutomaticDocumentBasedInvalidation;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;

/**
 * @author David Chevrier
 */
@Name("taskActions")
@Scope(ScopeType.CONVERSATION)
@AutomaticDocumentBasedInvalidation
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeTaskActionsBean extends TaskActionsBean {

    private static final long serialVersionUID = 3730629195809387779L;

    public boolean hasNuxeoPublishTaskPending() throws ClientException {
        boolean hasNuxeoPublicationTask = false;
        super.tasks = null;
        /* getCurrentDocumentTasks() returns tasks for current user and user of its groups*/
        List<Task> currentDocumentTasks = getCurrentDocumentTasks();
        if (currentDocumentTasks != null && !currentDocumentTasks.isEmpty()) {
            int index = 0;
            while (index < currentDocumentTasks.size() && !hasNuxeoPublicationTask) {
                Task task = currentDocumentTasks.get(index);
                if (CoreProxyWithWorkflowFactory.TASK_NAME.equals(task.getName())) {
                    hasNuxeoPublicationTask = ((Boolean) task.isOpened()).booleanValue();
                }
                index++;
            }
        }
        return hasNuxeoPublicationTask;
    }

    /**
     * Determine si l'action "workflow_online_accept" de la vue 'summary' doit
     * être présentée.
     * 
     * @return true si l'action doit être présentée. false sinon.
     * @throws ClientException
     */
    public boolean isValidateOnlineActionAuthorized() throws ClientException {
        return isTaskActionAuthorized(ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE);
    }

    protected boolean isTaskActionAuthorized(String taskName) throws ClientException {
        boolean isAuthorized = false;
        super.tasks = null;
        List<Task> currentDocumentTasks = getCurrentDocumentTasks();// get tasks for current users and users of its groups
        if (currentDocumentTasks != null) {
            NuxeoPrincipal nxPrincipal = (NuxeoPrincipal) documentManager.getPrincipal();
            
//            for (Task task : currentDocumentTasks) {
//                if (task.isOpened() && taskName.equals(task.getName())) {
//                    List<String> actors = task.getActors();
//                    boolean nxpIsActor = false;
//                    if (actors != null && !actors.isEmpty()) {
//                        nxpIsActor = actors.contains(nxPrincipal.getName());
//                    }
//                    isAuthorized = nxPrincipal.isAdministrator() || nxpIsActor;
//                }
//            }
            
            int index = 0;
            while (index < currentDocumentTasks.size() && !isAuthorized){
                Task task = currentDocumentTasks.get(index);
                if (task.isOpened() && ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE.equals(task.getName())) {
                    isAuthorized = true;
                }
            }
            
        }
        return isAuthorized;
    }

    public String getValidateOnlineTaskName() {
        return ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE;
    }

    public Task getValidateOnlineTask() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();
        return ToutaticeWorkflowHelper.getDocumentTaskByName(ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE, documentManager, currentDocument);
    }

    public boolean isValidateOnLineTask(Task task) throws ClientException {
        if (task != null) {
            return ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE.equals(task.getName());
        }
        return false;
    }

}
