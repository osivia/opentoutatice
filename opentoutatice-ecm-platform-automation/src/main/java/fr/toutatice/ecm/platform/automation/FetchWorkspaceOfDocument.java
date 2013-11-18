/**
 * Get the workspace of a given document.
 */
package fr.toutatice.ecm.platform.automation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

@Operation(id = FetchWorkspaceOfDocument.ID, category = Constants.CAT_FETCH, label = "Fetch workspace of document", description = "Fetch the workspace of a given document.")
public class FetchWorkspaceOfDocument {

	/**
	 * Id Nuxeo de l'opération (s'applique à un Document).
	 */
	public static final String ID = "Document.FetchWorkspaceOfDocument";

	private static final Log log = LogFactory.getLog(FetchWorkspaceOfDocument.class);
	/**
	 * Session "avec le coeur" de Nuxeo.
	 */
	@Context
	protected CoreSession coreSession;
	/**
	 * Document en entrée dont on cherche le workspace le contenant.
	 */
	@Param(name = "document", required = true)
	protected DocumentModel document;

	/**
	 * Classe permettant de filtrer les documents de type workspace.
	 */
	protected class WorksapceDocumentFilter implements Filter {

		private static final long serialVersionUID = 5364940781195839390L;

		public String WORKSPACE_TYPE = "Workspace";
		public String USER_WORKSPACE_TYPE = "UserWorkspace";

		@Override
		public boolean accept(DocumentModel document) {
			return WORKSPACE_TYPE.equals(document.getType()) || USER_WORKSPACE_TYPE.equals(document.getType());
		}

	}

	@OperationMethod
	public Object run() throws Exception {
		DocumentModel workspace = null;
		/* Vérifier que le document courant n'est pas lui même un workspace */
		Filter filter = new WorksapceDocumentFilter();
		if (filter.accept(document)) {
			workspace = document;
		} else {
			/*
			 * remonter la hiérarchie (opération réalisée en mode restricted
			 * afin de s'assurer que l'utilisateur connecté possède bien une
			 * visibilité sur les parents)
			 */
			DocumentModelList parentList = ToutaticeDocumentHelper.getParentList(coreSession, document, filter, true);
			if (parentList != null && parentList.size() > 0) {
				/* prendre le parent direct */
				workspace = parentList.get(0);
				if (!coreSession.hasPermission(workspace.getRef(), SecurityConstants.READ)) {
					throw new DocumentSecurityException(document.getPathAsString());
				}
			} else {
				throw new NoSuchDocumentException(document.getPathAsString());
			}
		}

		return workspace;
	}

}
