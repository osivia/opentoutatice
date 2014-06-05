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
import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.routing.api.DocumentRoute;
import org.nuxeo.ecm.platform.routing.core.api.DocumentRoutingEngineService;
import org.nuxeo.ecm.platform.routing.web.DocumentRoutingActionsBean;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.ecm.platform.task.TaskEventNames;
import org.nuxeo.ecm.webapp.helpers.EventNames;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;

/**
 * @author David Chevrier
 * 
 */
@Name("routingActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeDocumentRoutingActionsBean extends
		DocumentRoutingActionsBean {

	private static final long serialVersionUID = 8176244997123301627L;

	private static final Log log = LogFactory
			.getLog(ToutaticeDocumentRoutingActionsBean.class);
	
	@In(create = true)
	ToutaticeTaskActionsBean taskActions;
	
	/* 'Fork' of startRoute() to select online workflow */
	public String startOnlineWorkflow() throws ClientException {
		DocumentModel onlineWf = getOnlineWorkflowModel();
		DocumentModel currentDoc = navigationContext.getCurrentDocument();
		List<String> currentDocIds = new ArrayList<String>(1);
		currentDocIds.add(currentDoc.getId());

		getDocumentRoutingService().createNewInstance(onlineWf.getName(),
				currentDocIds, documentManager, true);
		
		/* Events for Observers and listeners */
		Events.instance().raiseEvent(EventNames.DOCUMENT_CHILDREN_CHANGED,
				onlineWf);
		
//		Task validateTask = getValidateTask();
//		ToutaticeWorkflowHelper.notifyRecipients(documentManager, validateTask,
//				currentDoc, null,
//				ToutaticeGlobalConst.CST_EVENT_ONLINE_TASK_APPROVED_ASSIGNED);
		
		/* UI Seam events */
		FacesMessages.instance().addFromResourceBundle(
				"toutatice.label.online.wf.started");

		webActions.resetTabList();
		return null;
	}
	
	private Task getValidateTask() throws ClientException{
		Task validate = null;
		List<Task> currentRouteAllTasks = getCurrentRouteAllTasks();
		int index = 0;
		while(index < currentRouteAllTasks.size() && validate == null){
			Task task = currentRouteAllTasks.get(index);
			if(ToutaticeGlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE.equalsIgnoreCase(task.getName())){
				validate = task;
			}
			index++;
		}
		return validate;
	}

	private DocumentModel getOnlineWorkflowModel() throws ClientException {
		String id = getDocumentRoutingService().getRouteModelDocIdWithId(
				documentManager,
				ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE);
		return getRouteModel(id);
	}

	/**
	 * Check if a workflow is running on current document.
	 * 
	 * @return true if process is running
	 * @throws ClientException
	 */
	public boolean isWorkflowRunning() throws ClientException {
		List<DocumentRoute> relatedRoutes = getRelatedRoutes();
		return relatedRoutes != null && !relatedRoutes.isEmpty();
	}

	/**
	 * Check if a process (DocumentRoute) is running on current selection list
	 * of documents.
	 * 
	 * @return true if process is running
	 * @throws ClientException
	 */
	public boolean isWorkflowRunningForSelection() throws ClientException {
		List<DocumentModel> currentDocumentSelection = documentsListsManager
				.getWorkingList(CURRENT_DOCUMENT_SELECTION);
		return isWorkflowRunningForSelection(currentDocumentSelection);
	}

	/**
	 * Check if a process (DocumentRoute) is running on selection list of
	 * documents.
	 * 
	 * @return true if process is running
	 * @throws ClientException
	 */
	private boolean isWorkflowRunningForSelection(List<DocumentModel> selection)
			throws ClientException {
		boolean status = false;
		for (DocumentModel document : selection) {
			if (isWorkflowRunningForDocument(document)) {
				status = true;
				break;
			}
		}
		return status;
	}

	public boolean isWorkflowRunningForDocument(DocumentModel document)
			throws ClientException {
		List<DocumentRoute> documentRoutes = getDocumentRoutingService()
				.getDocumentRoutesForAttachedDocument(documentManager,
						document.getId());
		return (documentRoutes != null && !documentRoutes.isEmpty());
	}

	/**
	 * Determine si l'action "workflow_online_cancel" de la vue 'summary' doit
	 * être présentée.
	 * 
	 * <h4>Conditions</h4> <li>Un process de demande de m doit exister</li> <li>
	 * l'utilisateur courant doit être l'initateur de ce processus</li>
	 * 
	 * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isCancelOnlineActionAuthorized() throws ClientException {
		boolean doWorkflowExist = false;
		boolean isUserInitiator = false;

		try {
			doWorkflowExist = isOnLineWorkflowRunning();
			NuxeoPrincipal principal = currentUser;
			isUserInitiator = principal.getName().equals(
					getCurrentWorkflowInitiator());
		} catch (Exception e) {
			log.debug("Failed to execute 'isCancelOnlineActionAuthorized()', error: "
					+ e.getMessage());
		}

		return doWorkflowExist && isUserInitiator;
	}

	public boolean isOnLineWorkflowRunning() {
		return getOnlineWorkflow() != null;
	}

	/**
	 * Determine si l'action "direct_online" de la vue 'summary' doit être
	 * présentée.
	 * 
	 * <h4>Conditions</h4> <li>(l'utilisateur courant doit avoir la permission
	 * de validation (rôle de validateur) - cf filtre action)</li> <li>le
	 * document doit être dans l'état 'projet'</li> <li>le document ne doit pas
	 * déjà être dans un processus de validation/mise en ligne (quels que soient
	 * les utilisateurs en charge de faire la validation)</li>
	 * 
	 * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isDirectSetOnlineActionAuthorized() throws ClientException {
		boolean isAuthorized = false;
		try {
			String currentLifeCycle = ((ToutaticeNavigationContext) navigationContext)
					.getCurrentLifeCycleState();
			boolean isInProjectState = (ToutaticeNuxeoStudioConst.CST_DOC_STATE_PROJECT
					.equals(currentLifeCycle)) ? true : false;
			boolean isInOnlineWf = isOnLineWorkflowRunning();
			isAuthorized = isInProjectState && !isInOnlineWf;
		} catch (Exception e) {
			log.debug("Failed to execute 'isDirectSetOnlineActionAuthorized()', error: "
					+ e.getMessage());
		}
		return isAuthorized;
	}

	public String getPendingWorkflowName() throws ClientException {
		/* Take the first route in the routes stack */
		String name = StringUtils.EMPTY;
		DocumentRoute relatedRoute = getRelatedRoute();
		if (relatedRoute != null) {
			name = relatedRoute.getName();
		} else {
			if (taskActions.hasNuxeoPublishTaskPending()) {
				name = "remote_publication_process";
			}
		}
		return name;
	}

	/* "Fork" of cancelRoute() */
	public String cancelOnlineWorkflow() throws ClientException {
		List<DocumentRoute> routes = getRelatedRoutes();
		if (routes.size() == 0) {
			log.error("No workflow to cancel");
			return null;
		}
		DocumentRoute route = getRunningWorkflowByName(routes,
				ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE);
		
		Task validateTask = getValidateTask();
		DocumentModel currentDoc = navigationContext.getCurrentDocument();
		ToutaticeWorkflowHelper.notifyRecipients(documentManager, validateTask,
				currentDoc, null,
				ToutaticeGlobalConst.CST_EVENT_ONLINE_WF_CANCELED);
		
		Framework.getLocalService(DocumentRoutingEngineService.class).cancel(
				route, documentManager);
		// force computing of tabs
		webActions.resetTabList();
		
		Events.instance().raiseEvent(TaskEventNames.WORKFLOW_CANCELED);
		
		Contexts.removeFromAllContexts("relatedRoutes");
		documentManager.save();
		
		return navigationContext.navigateToDocument(navigationContext
				.getCurrentDocument());
	}

	public DocumentRoute getOnlineWorkflow() {
		List<DocumentRoute> routes = getRelatedRoutes();
		return getRunningWorkflowByName(routes,
				ToutaticeGlobalConst.CST_WORKFLOW_PROCESS_ONLINE);
	}

	public DocumentRoute getRunningWorkflowByName(List<DocumentRoute> routes,
			String workflowName) {
		DocumentRoute onlineRoute = null;
		if (routes != null) {
			int index = 0;
			while (index < routes.size() && onlineRoute == null) {
				DocumentRoute route = routes.get(index);
				if (workflowName.equals(route.getName())) {
					onlineRoute = route;
				}
				index++;
			}
		}
		return onlineRoute;
	}

}
