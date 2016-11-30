/**
 * 
 */
package fr.toutatice.ecm.platform.automation.trash;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.trash.TrashService;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
@Operation(id = RestoreDocuments.ID, category = Constants.CAT_SERVICES, label = "RestoreDocuments", description = "Restore (selected) documents in trash.")
public class RestoreDocuments {

   public static final String ID = "Services.RestoreDocuments";
   
   @Context
   protected CoreSession session;
   
   @Param(name = "parent", required = false)
   protected DocumentModel parent;

   @OperationMethod
   public void run(DocumentModelList documents) throws Exception {
       TrashService trashService = Framework.getService(TrashService.class);
       trashService.undeleteDocuments(documents);
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
