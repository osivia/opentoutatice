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
package fr.toutatice.ecm.platform.core.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ec.notification.NotificationConstants;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingConstants;
import org.nuxeo.ecm.platform.routing.api.DocumentRoutingService;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.core.service.DocumentTaskProvider;
import org.nuxeo.ecm.platform.task.core.service.TaskEventNotificationHelper;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

/**
 * @author David Chevrier
 * 
 */
public final class ToutaticeWorkflowHelper {

    public static final String GET_TASKS_BY_NAME_PROVIDER = "GET_TASKS_BY_NAME_FOR_TARGET_DOCUMENT";
    public static final String GET_WF_BY_NAME_QUERY = "select * from DocumentRoute where dc:title = '%s' and docri:participatingDocuments = '%s' "
            + "and ecm:currentLifeCycleState IN ('ready','running') order by dc:created";


    private ToutaticeWorkflowHelper() {
    }

    /**
     * Get task by name for current document.
     * 
     * @param taskName
     * @param session
     * @param document
     * @return task given name and current doc
     * @throws ClientException
     */
    public static Task getTaskByName(String taskName, CoreSession session, DocumentModel currentDoc) throws ClientException {
        Task searchedTask = null;

        Object[] params = {taskName, currentDoc.getId()};
        List<Task> tasks = DocumentTaskProvider.getTasks(GET_TASKS_BY_NAME_PROVIDER, session, true, null, params);

        if (CollectionUtils.isNotEmpty(tasks)) {
            searchedTask = tasks.get(0);
        }

        return searchedTask;

    }
    
    /**
     * @param taskName
     * @return true if Task of given name is pending
     */
    public static boolean isTaskPending(String taskName, CoreSession session, DocumentModel currentDoc){
        boolean isPending = false;
        
        Task taskByName = getTaskByName(taskName, session, currentDoc);
        if(taskByName != null){
            isPending = taskByName.isOpened();
        }
        
        return isPending;
    }
    
    /**
     * Get workflow (or Route) by name on current document.
     * 
     * @param workflowName
     * @param currentDoc
     * @return workflow given name and current doc
     */
    public static DocumentRoute getWorkflowByName(String workflowName, DocumentModel currentDoc) {
        DocumentRoute searchedWf = null;
        
        CoreSession session = currentDoc.getCoreSession();
        String query = String.format(GET_WF_BY_NAME_QUERY, workflowName, currentDoc.getId());
        
        ToutaticeQueryHelper.UnrestrictedQueryRunner queryRunner = new ToutaticeQueryHelper.UnrestrictedQueryRunner(session, query);
        DocumentModelList wfs = queryRunner.runQuery();
        
        if(CollectionUtils.isNotEmpty(wfs)){
            DocumentModel wf = wfs.get(0);
            searchedWf = wf.getAdapter(DocumentRoute.class);
        }

        return searchedWf;
    }

    public static void notifyRecipients(CoreSession documentManager, Task task, DocumentModel document, String initiator, String event)
            throws ClientException {
        NuxeoPrincipal principal = (NuxeoPrincipal) documentManager.getPrincipal();

        Map<String, Serializable> eventProperties = new HashMap<String, Serializable>();
        ArrayList<String> notificationRecipients = new ArrayList<String>();
        if (initiator != null) {
            notificationRecipients.add(initiator);
        }
        notificationRecipients.addAll(task.getActors());
        eventProperties.put(NotificationConstants.RECIPIENTS_KEY, notificationRecipients.toArray(new String[notificationRecipients.size()]));

        TaskEventNotificationHelper.notifyEvent(documentManager, document, principal, task, event, eventProperties, null, null);
    }

    public static DocumentRoute getOnLineWorkflow(DocumentModel currentDoc) {
        return getWorkflowByName(ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE, currentDoc);
    }

    public static boolean isOnLineWorkflow(DocumentModel currentDoc) {
        return getOnLineWorkflow(currentDoc) != null;
    }

    public static String getOnLineWorkflowInitiator(DocumentModel currentDoc) throws PropertyException, ClientException {
        String initiator = StringUtils.EMPTY;
        DocumentRoute route = getOnLineWorkflow(currentDoc);
        if (route != null) {
            initiator = (String) route.getDocument().getPropertyValue(DocumentRoutingConstants.INITIATOR);
        }
        return initiator;
    }
    
    /**
     * @param currenDoc
     * @return initiator of current workflow on current document.
     */
    public static String getCurrentWorkflowInitiator(DocumentModel currentDoc){
        String initiator = StringUtils.EMPTY;
        
        DocumentRoutingService routing = (DocumentRoutingService) Framework.getService(DocumentRoutingService.class);
        currentDoc.getSessionId();
        List<DocumentRoute> documentRoutesForAttachedDocument = routing.getDocumentRoutesForAttachedDocument(currentDoc.getCoreSession(), currentDoc.getId());
        
        if(CollectionUtils.isNotEmpty(documentRoutesForAttachedDocument)){
            DocumentModel documentModel = documentRoutesForAttachedDocument.get(0).getDocument();
            initiator = (String) documentModel.getPropertyValue(DocumentRoutingConstants.INITIATOR);
        }
        
        return initiator;
    }
    
}
