package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


@Operation(id = EraseModifications.ID, category = Constants.CAT_DOCUMENT, label = "Erase unpublished modifications of live document yet locally published",
        description = "Erase unpublished modifications of live document yet locally published. This live is given as input")
public class EraseModifications {
    
    /** Identifiant of operation. */
    public static final String ID = "Document.EraseModifications";

    @Context
    protected CoreSession session;

    @Context
    protected OperationContext ctx;

    @Param(name = "skipCreateVersion", required = false, values = "false")
    protected boolean skipCreateVersion = false;

    @Param(name = "skipCheckout", required = false, values = "false")
    protected boolean skipCheckout = false;

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentRef docRef) throws Exception {
        
        final DocumentModel document = session.getDocument(docRef);
        // It must be a live document
        if(!document.isProxy() && !document.isVersion()){
            DocumentModel publishedDocument = ToutaticeDocumentHelper.getProxy(session, document, SecurityConstants.READ);  
            
            if(publishedDocument != null){
                
                DocumentModel publishedVersion = session.getSourceDocument(publishedDocument.getRef());
                session.restoreToVersion(document.getRef(), publishedVersion.getRef(), skipCreateVersion, skipCheckout);
                
            } else {
                throw new Exception("Input live document is not yet locally published.");
            }
        } else {
            throw new Exception("Input document is not a working copy.");
        }
        
        // The restored version keeps the same id
        return session.getDocument(docRef);
        
    }

}
