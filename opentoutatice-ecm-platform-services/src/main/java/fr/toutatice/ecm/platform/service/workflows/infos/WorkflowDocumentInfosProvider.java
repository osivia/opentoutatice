/**
 * 
 */
package fr.toutatice.ecm.platform.service.workflows.infos;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;
import fr.toutatice.ecm.platform.service.workflows.ToutaticeWorkflowService;


/**
 * @author david
 *
 */
public class WorkflowDocumentInfosProvider implements DocumentInformationsProvider {
    
    /** Toutatice workflow service. */
    // WorkflowDocumentInfosProvider is called after ToutaticeWorkflowService definition
    private ToutaticeWorkflowService wfSrv = (ToutaticeWorkflowService) Framework.getService(ToutaticeWorkflowService.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws NuxeoException {
        Map<String, Object> infos = new HashMap<String, Object>(0);
        // No workflow on Folderish
        if(!currentDocument.isFolder()){
            if (!this.wfSrv.hasContributions()) {
                infos.put(ToutaticeWorkflowService.VALIDATION_WF_RUNNING_INFOS_KEY, 
                        this.wfSrv.isWorkflowOfCategoryRunning(ToutaticeWorkflowService.VALIDATION_WF_CATEGORY, currentDocument));
            } else {
                infos.put(ToutaticeWorkflowService.VALIDATION_WF_RUNNING_INFOS_KEY, false);
            }
        }

        return infos;
    }

}
