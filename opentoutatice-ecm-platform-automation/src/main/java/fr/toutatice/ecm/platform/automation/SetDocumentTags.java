package fr.toutatice.ecm.platform.automation;

import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.StringList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;

@Operation(id = SetDocumentTags.ID, category = Constants.CAT_DOCUMENT, label = "SetDocumentTags", description = "Sets a document tags. Replace the previous list of tags by the list.")
public class SetDocumentTags {

	public static final String ID = "SetDocumentTags";

	private static final Log log = LogFactory.getLog(SetDocumentTags.class);

	@Context
	protected CoreSession session;

	@Param(name = "labels", required = true, description = "list of tag labels")
	protected StringList labels;

	@Param(name = "username", required = true)
	protected String username;

	@OperationMethod
	public void run(DocumentModel input) throws Exception {
		final TagService service = Framework.getLocalService(TagService.class);

		final String docId = input.getId();

		log.info("SetDocumentTags> docId=" + docId + " tags=" + StringUtils.join(labels, ", ") + " username=" + username);

		// remove existing tags
		final List<Tag> existingTags = service.getDocumentTags(session, docId, username);

		for (final Tag tag : existingTags) {
			service.untag(session, docId, tag.getLabel(), username);
		}

		// add new tags
		for (final String label : labels) {
			service.tag(session, docId, label, username);
		}

		session.save();
	}

}
