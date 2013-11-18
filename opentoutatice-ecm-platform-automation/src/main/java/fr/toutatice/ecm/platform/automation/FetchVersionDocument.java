package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.VersionModelImpl;

@Operation(id = FetchVersionDocument.ID, category = Constants.CAT_FETCH, label = "Version Document", description = "Fetch the live document from the repository given its reference (path or UID). Find the live document associated with the proxy document passed as parameter 'value'. Check the user permissions against this document The document will become the input of the next operation.")
public class FetchVersionDocument {

	public static final String ID = "Document.FetchVersionDocument";

	@Context
	protected CoreSession session;

	@OperationMethod(collector=DocumentModelCollector.class)
	public DocumentModel run(DocumentModel doc) throws Exception {
		
		DocumentModel vdoc = doc;
		if (!doc.isVersion()) {
			String label = doc.getVersionLabel();
			VersionModelImpl vm = new VersionModelImpl();
			vm.setLabel(label);
			vdoc = session.getDocumentWithVersion(doc.getRef(), vm);
		}
		
		return vdoc;
	}
}
