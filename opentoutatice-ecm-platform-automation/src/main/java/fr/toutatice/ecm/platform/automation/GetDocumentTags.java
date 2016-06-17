package fr.toutatice.ecm.platform.automation;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;
import net.sf.json.JSONArray;

@Operation(id = GetDocumentTags.ID, category = Constants.CAT_DOCUMENT, label = "GetDocumentTags", description = "Fetch the document's list of tags. If username is set, only the tags applied by the user are returned. Returns a json array containing the tag labels (string values)")
public class GetDocumentTags {

	public static final String ID = "GetDocumentTags";

	private static final Log log = LogFactory.getLog(GetDocumentTags.class);

	@Context
	protected CoreSession session;

	@Param(name = "username", description = "user who applied the tags", required = false)
	protected String username;

	@OperationMethod
	public Blob run(DocumentModel input) throws Exception {
		final TagService service = Framework.getLocalService(TagService.class);

		final String docId = input.getId();

		log.info(ID + "> docId=" + docId);

		// remove existing tags
		final List<Tag> existingTags = service.getDocumentTags(session, docId, username);

		final JSONArray jsonTags = new JSONArray();
		for (final Tag tag : existingTags) {
			jsonTags.add(tag.getLabel());
		}

		if (log.isDebugEnabled()) {
			log.debug(ID + "> JSON: " + jsonTags);
		}

		return new StringBlob(jsonTags.toString(), "application/json");
	}

}
