package fr.toutatice.ecm.platform.automation;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

@Operation(id = RemoveFacets.ID, category = Constants.CAT_DOCUMENT, label = "Retrait de facets", description = "")
public class RemoveFacets {

	public static final String ID = "Document.RemoveFacets";

	@Context
	protected CoreSession session;

	/**
	 * liste des facets à retirer séparées par ,
	 */
	@Param(name = "facets")
	protected String facets;

	/**
	 * @param doc
	 *            le document auquel sont ajouté les facets
	 * @return un objet JSON
	 */
	@OperationMethod
	public DocumentModel run(final DocumentModel doc) {
		final String[] tab = facets.split(",");
		for (final String facet : tab) {
			doc.removeFacet(facet);
		}
		return session.saveDocument(doc);
	}
}
