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

import java.util.Map;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.task.Task;


/**
 * @author David Chevrier.
 *
 */
public interface ToutaticeTaskService {

    /**
     * Indicates if service has contributions.
     * 
     * @return
     */
    boolean hasContributions();

    /**
     * Getter for tasks contributions.
     * 
     * @return Map<String, String>
     */
    Map<String, String> getTaskContributions();

    /**
     * Indicates if given task is pending.
     * 
     * @param task
     * @return
     * @throws NuxeoException
     */
    boolean isTaskPending(Task task) throws NuxeoException;

    /**
     * Checks if user, associated with session, is initiator of task.
     * 
     * @param coreSession
     * @param task
     * @return
     * @throws NuxeoException
     */
    boolean isUserTaskInitiator(CoreSession coreSession, Task task) throws NuxeoException;

    /**
     * Get user validate right on document.
     * 
     * @throws ServeurException
     * @throws NuxeoException
     */
    public boolean canUserManageTask(CoreSession coreSession, Task currentTask, DocumentModel currentDocument, String permission) throws NuxeoException;

}
