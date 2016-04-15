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


@Operation(id = EraseModifications.ID, category = Constants.CAT_DOCUMENT, label = "Checks in a new version from the published document",
        description = "Checks in a new version from the published document. Returns the checked in document.")
public class EraseModifications {

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

        final DocumentModel live = session.getDocument(docRef);

        DocumentModel publishedDocument = ToutaticeDocumentHelper.getProxy(session, live, SecurityConstants.READ);

        final DocumentModel latestLive = session.getWorkingCopy(publishedDocument.getRef());
        final DocumentModel sourceDocument = session.getSourceDocument(publishedDocument.getRef());

        session.restoreToVersion(latestLive.getRef(), sourceDocument.getRef(), skipCreateVersion, skipCheckout);

        return session.getDocument(docRef);
    }

}
