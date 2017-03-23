/**
 * 
 */
package fr.toutatice.ecm.platform.service.workflows.infos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.task.Task;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.helper.ToutaticeWorkflowHelper;
import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;
import fr.toutatice.ecm.platform.service.workflows.ToutaticeTaskService;


/**
 * @author david
 *
 */
public class TaskDocumentInfosProvider implements DocumentInformationsProvider {
    
    /** Toutatice tasks service. */
    // TaskDocumentInfosProvider is called after ToutaticeTaskService definition
    private ToutaticeTaskService taskSrv = (ToutaticeTaskService) Framework.getService(ToutaticeTaskService.class);
    
    /**
     * Informations given by service on given Task.
     */
    private enum TaskInfos {
        taskName, isTaskPending, canManageTask, isTaskInitiator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        Map<String, Object> infos = new HashMap<String, Object>(0);
        
        // No Task on Folderish
        if(!currentDocument.isFolder()){
        
            if (this.taskSrv.hasContributions()) {
                // At one time, there is only one task on current document 
                Task currentTask = null;
                boolean taskFound = false;
                String searchedTaskName = StringUtils.EMPTY;
                String permission = null;
                
                Set<String> tasksNames = this.taskSrv.getTaskContributions().keySet();
                Iterator<String> iterator = tasksNames.iterator();
                
                while (iterator.hasNext() && !taskFound) {
                    String taskName = iterator.next();
                    Task task = getTask(coreSession, currentDocument, taskName);
                    if (task != null) {
                        searchedTaskName = taskName;
                        currentTask = task;
                        permission = this.taskSrv.getTaskContributions().get(taskName);
                        taskFound = true;
                    }
                }
                
                infos.put(TaskInfos.taskName.name(), searchedTaskName);
                infos.put(TaskInfos.isTaskPending.name(), this.taskSrv.isTaskPending(currentTask));
                infos.put(TaskInfos.isTaskInitiator.name(), this.taskSrv.isUserTaskInitiator(coreSession, currentTask));
                infos.put(TaskInfos.canManageTask.name(), this.taskSrv.canUserManageTask(coreSession, currentTask, currentDocument, permission));
                
            }
        
        }
        return infos;
    }
    
    /**
     * Gets task.
     * 
     * @param coreSession
     * @param document
     * @param taskName
     * @return Task
     */
    protected Task getTask(CoreSession coreSession, DocumentModel document, String taskName) {
        return ToutaticeWorkflowHelper.getTaskByName(taskName, coreSession, document);
    }

}
