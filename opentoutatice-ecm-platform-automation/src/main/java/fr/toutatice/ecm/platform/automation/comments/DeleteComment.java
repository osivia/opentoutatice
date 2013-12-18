package fr.toutatice.ecm.platform.automation.comments;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

@Operation(id = DeleteComment.ID, category = Constants.CAT_DOCUMENT, label = "DeleteCommentOfDocument", description = "Delete a comment of a (commentable) document")
public class DeleteComment {
	
public static final String ID = "Document.DeleteComment";

	@Param(name = "commentableDoc", required = true)
	protected DocumentModel document;
	
	@Param(name = "comment", required = true)
	protected DocumentModel comment;

	@OperationMethod
	public Object run() throws ClientException {
		
		if (document.hasFacet("Commentable")) {
			CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
			commentableDoc.removeComment(comment);
		}
		return document;
		
	}
}
