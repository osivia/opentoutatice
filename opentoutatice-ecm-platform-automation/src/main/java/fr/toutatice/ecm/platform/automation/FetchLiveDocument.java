package fr.toutatice.ecm.platform.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;

@Operation(id = FetchLiveDocument.ID, category = Constants.CAT_FETCH, label = "FetchLiveDocument", description = "Fetch the live document from the repository given its reference (path or UID). Find the live document associated with the proxy document passed as parameter 'value'. Check the user permissions against this document. The document will become the input of the next operation.")
public class FetchLiveDocument {
	private static final String AND = "AND";

	private static final String OR = "OR";

	public static final String ID = "Document.FetchLiveDocument";

	private static final Log log = LogFactory.getLog(FetchLiveDocument.class);
	
    @Context
	protected CoreSession session;

	@Param(name = "value", required = true)
	protected DocumentModel value;

	@Param(name = "permission", required = false, values = {SecurityConstants.READ, SecurityConstants.READ_WRITE, SecurityConstants.WRITE})
	protected String permission = SecurityConstants.WRITE;
	
	@Param(name= "operation", required = false, values = {OR,AND})
	protected String operation = OR;
	
	@OperationMethod
	public Object run() throws Exception {

		DocumentModel liveDocument = null;

		// retrouver la verison live du document passé en paramètre
		UnrestrictedFecthLiveRunner runner = new UnrestrictedFecthLiveRunner(session, value);
		runner.run();
		liveDocument = runner.getLiveDocument();

		// récupérer la liste des permissions à verifier
		String[] tabPermissions = permission.split(",");

		if (null == liveDocument) {
			// le document live n'existe pas/plus
			throw new NoSuchDocumentException(value.getPathAsString());
		}
		boolean isAllowed = true;
		for (String perm : tabPermissions) {
			if (OR.equalsIgnoreCase(operation)) {
				isAllowed = session.hasPermission(liveDocument.getRef(), perm);
				if (isAllowed) {
					break;
				}
			} else if (AND.equalsIgnoreCase(operation)) {
				isAllowed = session.hasPermission(liveDocument.getRef(), perm);
				if (!isAllowed) {
					break;
				}
			}
		}
		// vérifier la permission
		if (!isAllowed) {
			throw new DocumentSecurityException(value.getPathAsString());
		}

		return liveDocument;
	}

	private static class UnrestrictedFecthLiveRunner extends UnrestrictedSessionRunner {

		private DocumentModel document;
		private DocumentModel liveDocument;

		public DocumentModel getLiveDocument() {
			return this.liveDocument;
		}

		public UnrestrictedFecthLiveRunner(CoreSession session, DocumentModel document) {
			super(session);
			this.document = document;
		}

		@Override
		public void run() throws ClientException {
			DocumentModel srcDocument = this.session.getSourceDocument(this.document.getRef());
			this.liveDocument = this.session.getWorkingCopy(srcDocument.getRef());
		}

	}

}
