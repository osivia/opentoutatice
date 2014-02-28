package fr.toutatice.ecm.platform.automation.comments;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

@Operation(id = CreateChildComment.ID, category = Constants.CAT_DOCUMENT, label = "CreateChildCommentOfDocument", description = "Add a child comment (answer) to a (commentable) document")
public class CreateChildComment {
	
public static final String ID = "Document.CreateChildComment";
	
	@Context
	protected CoreSession session;
	
	@Param(name = "commentableDoc", required = true)
	protected DocumentModel document;

	@Param(name = "comment", required = true)
	protected DocumentModel comment;
	
	@Param(name = "childComment", required = true)
	protected String childCommentContent;

	@OperationMethod
	public Object run() throws ClientException {

		CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
		DocumentModel childComment = AddComment.createComment(document.getRef(), document.getType(), session, childCommentContent);
		DocumentModel commentDoc = commentableDoc.addComment(comment, childComment);
		if(AddComment.THREAD_TYPE.equals(document.getType())){
            Boolean isModerated = (Boolean) document.getProperty("thread", "moderated");
            if(!isModerated){
                session.followTransition(commentDoc.getRef(), AddComment.PUBLISHED_TRANSITION);
            }
        }
		return document;

	}

}
