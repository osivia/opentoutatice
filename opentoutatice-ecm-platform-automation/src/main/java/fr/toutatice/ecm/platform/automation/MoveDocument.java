/**
 * 
 */
package fr.toutatice.ecm.platform.automation;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.AbstractSession;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.model.Session;
import org.nuxeo.ecm.core.storage.sql.Node;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLDocument;

import fr.toutatice.ecm.platform.core.constants.ToutaticeGlobalConst;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david
 *
 */
@Operation(id = MoveDocument.ID, category = Constants.CAT_DOCUMENT, label = "Move", description = "Move the input document into the target folder. Manage associated proxies if any.")
public class MoveDocument {

    public static final String ID = "Document.Move";
    
    /** Default conversation id. */
    public static final String CONVERSATION_ID = "0NXMAIN0";
    
    @Context
    protected OperationContext ctx;

    @Context
    protected CoreSession coreSession;

    @Param(name = "target")
    protected DocumentRef target; // the path or the ID

    @Param(name = "name", required = false)
    protected String name;

    @OperationMethod(collector=DocumentModelCollector.class)
    public DocumentModel run(DocumentModel doc) throws Exception {
        
        moveDocRestoringState(doc);
        
        return doc;
    }


    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentRef ref) throws Exception {

        DocumentModel document = coreSession.getDocument(ref);

        moveDocRestoringState(document);
        
        return document;
    }


    /**
     * Move document restoring its initial state if necessary.
     * 
     * @param ref
     * @param document
     */
    private void moveDocRestoringState(DocumentModel document) {
        DocumentRef ref = document.getRef();

        String n = name;
        if (name == null || name.length() == 0) {
            n = document.getName();
        }

        if (ToutaticeDocumentHelper.isRemoteProxy(document)) {
            coreSession.move(ref, target, n);
        } else if(ToutaticeDocumentHelper.isLocaProxy(document)){
            DocumentModel workingCopy = coreSession.getWorkingCopy(ref);
            n = StringUtils.substringBeforeLast(n, ToutaticeGlobalConst.CST_PROXY_NAME_SUFFIX);
            moveNRestore(workingCopy, n, true);
        } else {
            moveNRestore(document, n, false);
        }
    }


    /**
     * @param document
     * @param ref
     * @param n
     */
    private void moveNRestore(DocumentModel document, String n, boolean addFacet) {
        
        boolean isbeingModified = ToutaticeDocumentHelper.isBeingModified(coreSession, document);

        DocumentModel movedDocument = coreSession.move(document.getRef(), target, n);

        if (!isbeingModified) {

            // We restore last version since
            // only parentId has changed in move process

            DocumentModel documentVersion = coreSession.getLastDocumentVersion(movedDocument.getRef());
            DocumentModel versionRestored = coreSession.restoreToVersion(movedDocument.getRef(), documentVersion.getRef(), true, true);
            if(addFacet){
                versionRestored.addFacet(ToutaticeNuxeoStudioConst.CST_FACET_LOCAL_LIVE);
                ToutaticeDocumentHelper.saveDocumentSilently(coreSession, versionRestored, true);
            }

            // To store document in base (?)
            versionRestored.refresh();

        }
    }
        
}

