/**
 * 
 */
package fr.toutatice.ecm.platform.automation.trash;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.trash.TrashService;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
@Operation(id = PurgeDocuments.ID, category = Constants.CAT_SERVICES, label = "PurgeDocuments", description = "Definitly delete (selected) documents in trash.")
public class PurgeDocuments {
    
    public static final String ID = "Services.PurgeDocuments";
    
    @Context
    protected CoreSession session;
    
    @Param(name = "parent", required = false)
    protected DocumentModel parent;

    @OperationMethod
    public void run(DocumentModelList documents) throws Exception {
        TrashService trashService = Framework.getService(TrashService.class);
        
        if(trashService.checkDeletePermOnParents(documents)) {
            List<DocumentRef> refs = new ArrayList<DocumentRef>(documents.size());
            for(DocumentModel doc : documents){
                refs.add(doc.getRef());
            }
            
            trashService.purgeDocuments(this.session, refs);
        } else {
            throw new Exception("You don't have enough write permissions to delete given documents");
        }
    }
    
    @OperationMethod
    public void run() throws Exception {
        if(this.parent != null){
            String parentId = this.parent.getId();
            
            String query = String.format(TrashHelper.TRASH_REQUEST, parentId);
            DocumentModelList trashedDocs = this.session.query(query);
            run(trashedDocs);
            
        } else {
            throw new Exception("Parameter 'parent' is not defined");
        }
    }
    
}
