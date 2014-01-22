package fr.toutatice.ecm.platform.web.userservices;

import static org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager.CURRENT_DOCUMENT_SELECTION;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.jbpm.JbpmEventNames;
import org.nuxeo.ecm.platform.jbpm.JbpmListFilter;
import org.nuxeo.ecm.platform.jbpm.core.helper.AbandonProcessUnrestricted;
import org.nuxeo.ecm.platform.jbpm.core.helper.EndProcessUnrestricted;
import org.nuxeo.ecm.platform.jbpm.web.JbpmActionsBean;
import org.nuxeo.ecm.platform.jbpm.web.JbpmHelper;
import org.nuxeo.ecm.webapp.documentsLists.DocumentsListsManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.GlobalConst;
import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;
import fr.toutatice.ecm.platform.web.annotations.Install;
import fr.toutatice.ecm.platform.web.context.ToutaticeNavigationContext;

@Name("jbpmActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.TOUTATICE)
public class ToutaticeJbpmActionsBean extends JbpmActionsBean {
	
	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(ToutaticeJbpmActionsBean.class);
	
	public static final String TOUTATICE_TASK_NAME = "org.nuxeo.ecm.platform.publisher.jbpm.CoreProxyWithWorkflowFactory";
	
    @In(required = false, create = true)
    protected transient DocumentsListsManager documentsListsManager;

	protected TaskNamesList taskNames = new TaskNamesList();
	
    /**
     * Vérifie si un process (jbpm) quelconque est en cours sur le document courant 
     * @return true si un process JBPM existe. Sinon, false.
     * @throws ClientException
     */
    public boolean isProcessRunning() throws ClientException {
    	DocumentModel currentDocument = navigationContext.getCurrentDocument();
    	ArrayList<DocumentModel> list = new ArrayList<DocumentModel>();
    	list.add(currentDocument);
    	return isProcessRunningForSelection(list);
    }

    /**
     * Vérifie si un process (jbpm) quelconque est en cours sur la sélection de documents 
     * @return true si un process JBPM existe. Sinon, false.
     * @throws ClientException
     */
	public boolean isProcessRunningForSelection() throws ClientException {
		List<DocumentModel> currentDocumentSelection = documentsListsManager.getWorkingList(CURRENT_DOCUMENT_SELECTION);
		return isProcessRunningForSelection(currentDocumentSelection);
	}

    /**
     * Vérifie si un process (jbpm) quelconque est en cours sur la sélection de documents 
     * @return true si un process JBPM existe. Sinon, false.
     * @throws ClientException
     */
	private boolean isProcessRunningForSelection(List<DocumentModel> selection) throws ClientException {
		boolean status = false;
		
		for (DocumentModel document : selection) {
			if (isPending(document, null) || isTaskPending(document, null)) {
				status = true;
				break;
			}
		}
		
		return status;
	}

    /**
     * Determine si l'action "workflow_validation_cancel" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     * 
     * <h4>Conditions</h4>
     * <li>Un process de demande de validation doit exister</li>
     * <li>l'utilisateur courant doit être l'initateur de ce processus</li>
     *  
     * @return true si l'action doit être présentée. false sinon.
     * @throws ClientException
     */
    public boolean isCancelActionAuthorized() throws ClientException {
    	boolean doProcessExist = false;
    	boolean isUserInitiator = false;
    	String cLCS = null;
		
		try {
			cLCS = ((ToutaticeNavigationContext) navigationContext).getCurrentLifeCycleState();
			
			ProcessInstance runningProcess = getCurrentProcess();
			if (null != runningProcess) {
				String name = runningProcess.getProcessDefinition().getName();
				if ( (GlobalConst.CST_WORKFLOW_PROCESS_VALIDATION_APPROBATION.equals(name))
				  || (GlobalConst.CST_WORKFLOW_PROCESS_LEGACY_VALIDATION_APPROBATION.equals(name))
				  || (GlobalConst.CST_WORKFLOW_PROCESS_VALIDATION_PARALLEL.equals(name)) ) {
					doProcessExist = true;
				}

				NuxeoPrincipal pal = currentUser;
				isUserInitiator =  pal.getName().equals(getCurrentProcessInitiator());
			}
		} catch (Exception e) {
			log.debug("Failed to execute 'isCancelActionAuthorized()', error: " + e.getMessage());
		}
		
		return (NuxeoStudioConst.CST_DOC_STATE_PROJECT.equals(cLCS)) ? doProcessExist && isUserInitiator : false;
    }

    /**
     * Determine si l'action "workflow_online_cancel" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     * 
     * <h4>Conditions</h4>
     * <li>Un process de demande de validation doit exister</li>
     * <li>l'utilisateur courant doit être l'initateur de ce processus</li>
     *  
     * @return true si l'action doit être présentée. false sinon.
     * @throws ClientException
     */
    public boolean isCancelOnlineActionAuthorized() throws ClientException {
    	boolean doProcessExist = false;
    	boolean isUserInitiator = false;
    	
    	try {
    		ProcessInstance runningProcess = getCurrentProcess();
    		if (null != runningProcess) {
    			String name = runningProcess.getProcessDefinition().getName();
    			if (GlobalConst.CST_WORKFLOW_PROCESS_ONLINE.equals(name)) {
    				doProcessExist = true;
    			}
    		}
    		
    		NuxeoPrincipal pal = currentUser;
    		isUserInitiator =  pal.getName().equals(getCurrentProcessInitiator());
    	} catch (Exception e) {
    		log.debug("Failed to execute 'isCancelOnlineActionAuthorized()', error: " + e.getMessage());
    	}
    	
		return doProcessExist && isUserInitiator;
    }

	/**
     * Determine si les actions "workflow_validation_accept/workflow_validation_reject" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doivent être présentées.
     *  
     * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isRejectOrValidateActionAuthorized() throws ClientException {
		String[] taskNames = new String[] {GlobalConst.CST_WORKFLOW_TASK_VALIDATE, GlobalConst.CST_WORKFLOW_TASK_LEGACY_VALIDATE};
		
		String cLCS = ((ToutaticeNavigationContext) navigationContext).getCurrentLifeCycleState();
		
		List<TaskInstance> taskList = getCurrentTasks(taskNames);
		if ((null != taskList) && (0 < taskList.size())) {
			for (TaskInstance task : taskList) {
				if (getCanEndTask(task)) {
					return (NuxeoStudioConst.CST_DOC_STATE_PROJECT.equals(cLCS)) ? true : false;
				}
			}
		}
		
		return false;
	 }

	/**
     * Determine si l'action "workflow_online_reject" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     *  
     * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isRejectOnlineActionAuthorized() throws ClientException {
		String[] taskNames = new String[] {GlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE};
		
		List<TaskInstance> taskList = getCurrentTasks(taskNames);
		if ((null != taskList) && (0 < taskList.size())) {
			for (TaskInstance task : taskList) {
				if (getCanEndTask(task)) {
					return true;
				}
			}
		}
		
		return false;
	 }

	/**
     * Determine si l'action "workflow_online_accept" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     *  
     * @return true si l'action doit être présentée. false sinon.
	 * @throws ClientException
	 */
	public boolean isValidateOnlineActionAuthorized() throws ClientException {
		String[] taskNames = new String[] {GlobalConst.CST_WORKFLOW_TASK_ONLINE_VALIDATE};
		
		List<TaskInstance> taskList = getCurrentTasks(taskNames);
		if ((null != taskList) && (0 < taskList.size())) {
			for (TaskInstance task : taskList) {
				if (getCanEndTask(task)) {
					return true;
				}
			}
		}
		
		return false;
	 }

	/**
     * Determine si l'action "direct_validation" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     * 
     * <h4>Conditions</h4>
     * <li>le document doit être dans l'état 'projet'</li> 
     * <li>l'utilisateur courant doit avoir la permission de validation (rôle de validateur)</li>
     * <li>le document ne doit pas déjà être dans un processus de validation/mise en ligne (quels que soient les utilisateurs en charge de faire la validation)</li> 
     *  
     * @return true si l'action doit être présentée. false sinon.
     * @throws ClientException
	 */
	public boolean isValidationActionAuthorized() throws ClientException {
		String cLCS = null;
		
    	try {
    		cLCS = ((ToutaticeNavigationContext) navigationContext).getCurrentLifeCycleState();
    		ProcessInstance runningProcess = getCurrentProcess();
    		if (null != runningProcess) {
    			return false;
    		}
    	} catch (Exception e) {
    		log.debug("Failed to execute 'isValidationActionAuthorized()', error: " + e.getMessage());
    	}
		
		return (NuxeoStudioConst.CST_DOC_STATE_PROJECT.equals(cLCS)) ? true : false;
	}
	
	/**
	 * Détermine si un processus, déterminé par le nom du "workflow", est en cours pour un document.
	 * @param le document
	 * @param le nom du workflow 
	 * @return true si un processus est en cours false sinon.
	 * @throws ClientException
	 */
    public boolean isPending(DocumentModel document, String wkflName) throws ClientException {
    	List<ProcessInstance> lstProcess = jbpmService.getProcessInstances(document, null, (StringUtils.isNotBlank(wkflName)) ? new WorkflowNameFilter(wkflName) : null);
    	return (lstProcess != null && !lstProcess.isEmpty());
    }

	/**
	 * Détermine si un processus, déterminé par le nom du "task", est en cours pour un document.
	 * @param le document
	 * @param le nom du workflow 
	 * @return true si un processus est en cours false sinon.
	 * @throws ClientException
	 */
    public boolean isTaskPending(DocumentModel document, String taskName) throws ClientException {
    	List<TaskInstance> lstTasks = jbpmService.getTaskInstances(document, null, (StringUtils.isNotBlank(taskName)) ? new TaskInstanceFilter(taskName) : null);
    	return (lstTasks != null && !lstTasks.isEmpty());
    }

	/**
	 * Récupérer le processus associé à un document.
	 * @param le document
	 * @return le nom du processus si un processus est en cours, null sinon.
	 * @throws ClientException
	 */
    public String getPendingProcessName(DocumentModel document) throws ClientException {
    	String pName = null;
    	
    	List<ProcessInstance> lstProcess = jbpmService.getProcessInstances(document, null, null);
    	if (lstProcess != null && !lstProcess.isEmpty()) {
    		ProcessDefinition pDef = lstProcess.get(0).getProcessDefinition();
    		pName = pDef.getName();
    	} else if (isTaskPending(document, TOUTATICE_TASK_NAME)) {
    		pName = "remote_publication_process";
    	}
    	
    	return pName;
    }

	/**
     * Determine si l'action "direct_online" (fichier 'acaren-actions-contrib.xml') de la vue 'summary' doit être présentée.
     * 
     * <h4>Conditions</h4>
     * <li>l'utilisateur courant doit avoir la permission de validation (rôle de validateur)</li>
     * <li>le document doit être dans l'état 'projet'</li> 
     * <li>le document ne doit pas déjà être dans un processus de validation/mise en ligne (quels que soient les utilisateurs en charge de faire la validation)</li> 
     *  
     * @return true si l'action doit être présentée. false sinon.
     * @throws ClientException
	 */
	public boolean isDirectSetOnlineActionAuthorized() throws ClientException {
		return isValidationActionAuthorized();
	}

	/**
	 * Surcharge de la méthode pour que l'initiateur du processus ne puisse pas valider s'il n'a pas les droits ad-hoc
	 * 
	 * @see org.nuxeo.ecm.platform.jbpm.web.JbpmActionsBean#getCanEndTask(org.jbpm.taskmgmt.exe.TaskInstance)
	 */
	@Override
	public boolean getCanEndTask(TaskInstance taskInstance)
	throws ClientException {
		if (taskInstance != null && (!taskInstance.isCancelled() && !taskInstance.hasEnded())) {
			JbpmHelper helper = new JbpmHelper();
			NuxeoPrincipal pal = currentUser;
			return pal.isAdministrator() || helper.isTaskAssignedToUser(taskInstance, pal);
		}
		return false;
	}

	/**
	 * Surcharge de la méthode pour que le document soit récupéré via le NavigationContext::getCurrentDocument() et non DocumentContextBoundActionBean::getCurrentDocument()
	 * (problème de notification lors de l'annulation d'un process)
	 * 
	 * @see org.nuxeo.ecm.platform.jbpm.web.JbpmActionsBean#notifyEventListeners(java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
    public void notifyEventListeners(String name, String comment,
            String[] recipients) throws ClientException {
		jbpmService.notifyEventListeners(name, comment, recipients, documentManager, currentUser, navigationContext.getCurrentDocument());
    }

	@Override
	public List<TaskInstance> getCurrentTasks(String... taskNames) throws ClientException {
		if (!this.taskNames.equals(taskNames)) {
			// force reloading of the current tasks since the requested tasks' names have changed
			this.taskNames.setValue(taskNames);
			this.currentTasks = null;
		}
		
		return super.getCurrentTasks(taskNames);
	}
	
	public String cancelCurrentProcess(String eventToRaise) throws ClientException {
        ProcessInstance currentProcess = getCurrentProcess();
        if (currentProcess != null) {
            // remove wf acls
            Long pid = Long.valueOf(currentProcess.getId());
            DocumentModel currentDoc = navigationContext.getCurrentDocument();
            if (currentDoc != null) {
            	AbandonProcessUnrestricted runner = new AbandonProcessUnrestricted(documentManager, currentDoc.getRef(), pid);
                runner.runUnrestricted();
            }

            // end process and tasks using unrestricted session
            List<TaskInstance> tis = jbpmService.getTaskInstances(
            		documentManager.getDocument(currentDoc.getRef()),
            		(NuxeoPrincipal) null, null);

            EndProcessUnrestricted endProcessRunner = new EndProcessUnrestricted(documentManager, tis);
            endProcessRunner.runUnrestricted();

            jbpmService.deleteProcessInstance(currentUser, pid);
            facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor.getMessages().get("workflowProcessCanceled"));
            notifyEventListeners(eventToRaise, 
            		userComment,
                    endProcessRunner.getRecipients().toArray(new String[] {}));
            Events.instance().raiseEvent(JbpmEventNames.WORKFLOW_CANCELED);
            resetCurrentData();
        }
        webActions.resetCurrentTab();		
		return null;
	}
	
    @Observer(value = { JbpmEventNames.WORKFLOW_ENDED,
            JbpmEventNames.WORKFLOW_NEW_STARTED,
            JbpmEventNames.WORKFLOW_TASK_START,
            JbpmEventNames.WORKFLOW_TASK_STOP,
            JbpmEventNames.WORKFLOW_TASK_REJECTED,
            JbpmEventNames.WORKFLOW_USER_ASSIGNMENT_CHANGED,
            JbpmEventNames.WORKFLOW_TASK_COMPLETED,
            JbpmEventNames.WORKFLOW_TASK_REMOVED,
            JbpmEventNames.WORK_ITEMS_LIST_LOADED,
            JbpmEventNames.WORKFLOW_TASKS_COMPUTED,
            JbpmEventNames.WORKFLOW_ABANDONED,
            JbpmEventNames.WORKFLOW_CANCELED}, create = false)
    public void refreshUI() throws ClientException {
    	returnToCurrentDocOrHome();
    	resetCurrentData();
    }

    /*
     * permet le rafraichissement de la page suite à l'evenement de 
     * modification du document de travail
     */
    @Observer(value = {EventNames.DOCUMENT_SELECTION_CHANGED}, create = false)
    public void refreshUIDocSelectChanged() throws ClientException {    	
    	resetCurrentData();
    }

	/**
	 * Inner class to handle a cache at current tasks level
	 */
	private class TaskNamesList {
		List<String> array;
		
		public TaskNamesList() {
			this.array = new ArrayList<String>();
		}

		public void setValue(String[] list) {
			this.array.clear();
			for (String item : list) {
				this.array.add(item);
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object listToCompare) {
			boolean status = false;
			
			if (listToCompare instanceof String[]) {
				String[] list = (String[])listToCompare;
				if (this.array.size() == list.length) {
					status = true;
					for (String item : list) {
						if (!this.array.contains(item)) {
							status = false;
							break;
						}
					}
				}
			} else if (listToCompare instanceof List) {
				status = this.array.containsAll((List<String>)listToCompare);
			}
			
			return status;
		}

		@Override
		public String toString() {
			return this.array.toString();
		}
		
	}
	
	/**
	 * Classe permettant de filtrer une liste de processus en fonction du nom du workflow
	 * @author soazig
	 *
	 */
	private class WorkflowNameFilter implements JbpmListFilter {

		private static final long serialVersionUID = 1L;
		private String wkflName;
	
		public WorkflowNameFilter(String wflName) {
			this.wkflName = wflName;
		}	

		@SuppressWarnings("unchecked")
		public <T> ArrayList<T> filter(JbpmContext jbpmContext,
            DocumentModel document, 
            ArrayList<T> list, 
            NuxeoPrincipal principal) {
			ArrayList<ProcessInstance> result = new ArrayList<ProcessInstance>();
			
			// pas de filtrage si pas de nom de workflow passé en paramètre
			if (StringUtils.isBlank(this.wkflName)) {
				return list; 
			}
			
			// filtrage
			for (T t : list) {
				ProcessInstance pi = (ProcessInstance) t;
				String name = pi.getProcessDefinition().getName();
				if (this.wkflName.equals(name)) {               
                    result.add(pi);               
				}
        	}
			
			return (ArrayList<T>) result;
		}	
	}	

	/**
	 * Classe permettant de filtrer une liste de "task instance" en fonction du nom
	 */
	private class TaskInstanceFilter implements JbpmListFilter {

		private static final long serialVersionUID = 1L;
		private String taskName;
	
		public TaskInstanceFilter(String taskName) {
			this.taskName = taskName;
		}	

		@SuppressWarnings("unchecked")
		public <T> ArrayList<T> filter(JbpmContext jbpmContext,
            DocumentModel document, 
            ArrayList<T> list, 
            NuxeoPrincipal principal) {
			ArrayList<TaskInstance> result = new ArrayList<TaskInstance>();
			
			// pas de filtrage si pas de nom passé en paramètre
			if (StringUtils.isBlank(this.taskName)) {
				return list; 
			}
			
			// filtrage
			for (T t : list) {
				TaskInstance ti = (TaskInstance) t;
				String name = ti.getName();
				if (this.taskName.equals(name)) {               
                    result.add(ti);               
				}
        	}
			
			return (ArrayList<T>) result;
		}	
	}	
}
