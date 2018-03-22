/**
 * 
 */
package fr.toutatice.ecm.platform.automation.document;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;


/**
 * @author david
 *
 */
@Operation(id = FetchDocument.ID)
public class FetchDocument {
    
    /** Identifier. */
    public static final String ID = "Fetch.Document";
    
    @Context
    protected CoreSession session;
    
    /** Id. */
    @Param(name = "id")
    private String id;
    
    @OperationMethod
    public DocumentModel run() throws Exception {
        return this.session.getDocument(new IdRef(this.id));
    }


}
