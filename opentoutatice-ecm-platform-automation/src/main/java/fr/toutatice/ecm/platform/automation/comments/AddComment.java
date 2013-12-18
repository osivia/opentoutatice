package fr.toutatice.ecm.platform.automation.comments;

import java.security.Principal;
import java.util.Calendar;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

@Operation(id = AddComment.ID, category = Constants.CAT_DOCUMENT, label = "AddCommentToDocument", description = "Add a comment to a (commentable) document")
public class AddComment {

	public static final String ID = "Document.AddComment";
	public static final String COMMENT_TYPE = "Comment";
	
	@Context
	protected CoreSession session;

	@Param(name = "commentableDoc", required = true)
	protected DocumentModel document;
	
	@Param(name = "comment", required = true)
	protected String commentContent;

	@OperationMethod
	public Object run() throws Exception {

		CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
		DocumentModel comment = createComment(session, commentContent);
		commentableDoc.addComment(comment);
		return document;

	}
	
	public static DocumentModel createComment(CoreSession session, String commentContent) throws ClientException{
		DocumentModel comment = session.createDocumentModel(COMMENT_TYPE);
		Principal user = session.getPrincipal();
		if(user == null){
			throw new ClientException("No author for comment.");
		}
        comment.setProperty("comment", "author", user.getName());
        comment.setProperty("comment", "text", commentContent);
        comment.setProperty("comment", "creationDate", Calendar.getInstance());
        return comment;
	}
	
}
