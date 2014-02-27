/**
 * 
 */
package fr.toutatice.ecm.platform.web.publication;

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



/**
 * @author David Chevrier
 */
@Name("taskActions")
@Scope(ScopeType.CONVERSATION)
@AutomaticDocumentBasedInvalidation
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeTaskActionsBean extends TaskActionsBean {
    
    private static final long serialVersionUID = 3730629195809387779L;
    
    public boolean isPendingPublishTask(DocumentModel document) throws ClientException{
        NuxeoPrincipal principal = null;
        List<Task> currentDocumentTasks = taskService.getTaskInstances(document, principal, documentManager);
        for(Task task : currentDocumentTasks){
           if(CoreProxyWithWorkflowFactory.TASK_NAME.equals(task.getName())){
               return ((Boolean) task.isOpened()).booleanValue();
           }
        }
        return false;
    }
    
    
    public String getPendingPublishTaskName() throws ClientException {  
        DocumentModel document = navigationContext.getCurrentDocument();
        NuxeoPrincipal principal = null;
        List<Task> currentDocumentTasks = taskService.getTaskInstances(document, principal, documentManager);
        for(Task task : currentDocumentTasks){
            if(CoreProxyWithWorkflowFactory.TASK_NAME.equals(task.getName())
                    && task.isOpened()){
                return "remote_publication_process";
            }
         }
        return null;
    }

}