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
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;

@Operation(id = SetDocumentTags.ID, category = Constants.CAT_DOCUMENT, label = "Set Document Tags", description = "Sets a document tags. Replace the previous list of tags by the list.")
public class SetDocumentTags {

	public static final String ID = "Document.SetTags";

	private static final Log log = LogFactory.getLog(SetDocumentTags.class);

	@Context
	protected CoreSession session;

	@Context
	protected TagService tagService;

	@Param(name = "labels", required = false, description = "list of tag labels, if empty removes all current tags")
	protected StringList labels;

	@Param(name = "username", required = true)
	protected String username;

	@OperationMethod
	public void run(DocumentModel doc) throws Exception {
		final String docId = doc.getId();

		setLabels(docId);
	}

	@OperationMethod
	public void run(IdRef idRef) throws Exception {
		setLabels(idRef.toString());
	}

	private void setLabels(final String docId) {
		log.info("SetDocumentTags> docId=" + docId + " tags=" + StringUtils.join(labels, ", ") + " username=" + username);

		// remove existing tags
		final List<Tag> existingTags = tagService.getDocumentTags(session, docId, username);

		if (existingTags != null) {
			for (final Tag tag : existingTags) {
				tagService.untag(session, docId, tag.getLabel(), username);
			}
		}

		// add new tags
		if (labels != null) {
			for (final String label : labels) {
				if (StringUtils.isNotBlank(label)) {
					tagService.tag(session, docId, label, username);
				}
			}
		}

		session.save();
	}

}
