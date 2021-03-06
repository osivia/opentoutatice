/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 * dchevrier
 */
package fr.toutatice.ecm.platform.service.workflows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.core.helpers.TaskActorsHelper;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;


/**
 * @author david chevrier
 *
 */
public class ToutaticeTaskServiceImpl extends DefaultComponent implements ToutaticeTaskService {

    private static final Log log = LogFactory.getLog(ToutaticeTaskServiceImpl.class);

    private static final String TASKS_EXT_POINT = "tasks";

    /** Map of contributed tasks (name / permission). */
    private Map<String, String> tasksContribs;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasContributions() {
        return MapUtils.isNotEmpty(this.tasksContribs);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getTaskContributions() {
        return this.tasksContribs;
    }

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        tasksContribs = new HashMap<String, String>(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (TASKS_EXT_POINT.equals(extensionPoint)) {
            TaskDescriptor taskDesc = (TaskDescriptor) contribution;
            String taskName = taskDesc.getTaskName();
            String permission = taskDesc.getPermission();
            if (StringUtils.isNotBlank(taskName)) {
                tasksContribs.put(taskName, permission);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        if (TASKS_EXT_POINT.equals(extensionPoint)) {
            TaskDescriptor taskDesc = (TaskDescriptor) contribution;
            String taskName = taskDesc.getTaskName();
            if (StringUtils.isNotBlank(taskName)) {
                tasksContribs.remove(taskName);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTaskPending(Task task) throws ClientException {
        boolean isPending = false;
        if (task != null) {
            isPending = task.isOpened();
        }
        return isPending;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserTaskInitiator(CoreSession coreSession, Task task) throws ClientException {
        boolean isInitiator = false;

        if (task != null) {
            /* Initiator of wf is task initiator too */
            String taskInitiator = task.getInitiator();
            if (StringUtils.isNotBlank(taskInitiator)) {

                NuxeoPrincipal currentUser = (NuxeoPrincipal) coreSession.getPrincipal();
                isInitiator = taskInitiator.equals(currentUser.getName());

            }
        }
        return isInitiator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canUserManageTask(CoreSession coreSession, Task currentTask, DocumentModel currentDocument, String permission) throws ClientException {

        boolean canValidate = false;
        boolean isActor = false;
        
        if (currentTask != null) {

            List<String> actors = currentTask.getActors();
            if (actors != null && !actors.isEmpty()) {

                NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();
                if (principal != null) {

                    List<String> taskActors = TaskActorsHelper.getTaskActors(principal);
                    isActor = CollectionUtils.isSubCollection(actors, taskActors);

                }
            }

            if (isActor) {
                canValidate = checkPermission(coreSession, currentDocument, permission);
            }

        } 
        return canValidate;
    }

    private Boolean checkPermission(CoreSession coreSession, DocumentModel currentDocument, String permission) throws ClientException {
        /* Case of no contributed permission. */
        if(StringUtils.isBlank(permission)){
            return Boolean.TRUE;
        }
        
        Boolean can = Boolean.FALSE;
        try {
            can = Boolean.valueOf(coreSession.hasPermission(currentDocument.getRef(), permission));
        } catch (ClientException e) {
            if (e instanceof DocumentSecurityException) {
                return Boolean.FALSE;
            } else {
                log.warn("Failed to fetch permissions for document '" + currentDocument.getPathAsString() + "', error:" + e.getMessage());
                throw new ClientException(e);
            }
        }
        return can;
    }

}
