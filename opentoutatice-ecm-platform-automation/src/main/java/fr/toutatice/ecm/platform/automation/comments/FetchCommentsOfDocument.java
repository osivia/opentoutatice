package fr.toutatice.ecm.platform.automation.comments;

import java.security.Principal;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

@Operation(id = FetchCommentsOfDocument.ID, category = Constants.CAT_FETCH, label = "FetchCommentsOfDocument", description = "Fetches comments of a (commentable) document")
public class FetchCommentsOfDocument {

	public static final String ID = "Fetch.DocumentComments";

	private static final Log log = LogFactory.getLog(FetchCommentsOfDocument.class);
	
	private static final String COMMENT_SCHEMA = "comment";
	private static final String POST_SCHEMA = "post";

	@Context
	CoreSession session;

	@Param(name = "commentableDoc", required = true)
	protected DocumentModel document;

	@OperationMethod
	public Object run() throws ClientException {

		JSONArray commentsTree = new JSONArray();
		/*
		 * Récupération du service de commentaires.
		 */
		CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
		String schemaPrefix = "comment";
		if(AddComment.THREAD_TYPE.equals(document.getType())){
		    schemaPrefix = "post";
		}
		List<DocumentModel> commentsRoots = commentableDoc.getComments();
		if (commentsRoots != null) {
			/*
			 * Construction de la liste des fils de commentaires.
			 */
			for (DocumentModel commentRoot : commentsRoots) {
				JSONObject jsonCommentRoot = new JSONObject();
				jsonCommentRoot.element("id", commentRoot.getId());
				String author = (String) commentRoot.getProperty(schemaPrefix, "author");
				jsonCommentRoot.element("author", author);
				jsonCommentRoot.element("creationDate", commentRoot.getProperty(schemaPrefix, "creationDate"));
				jsonCommentRoot.element("content", commentRoot.getProperty(schemaPrefix, "text"));
				jsonCommentRoot.element("modifiedDate", commentRoot.getProperty("dublincore", "modified"));
				boolean canDelete = canDeleteComment(author);
				jsonCommentRoot.element("canDelete", canDelete);
				jsonCommentRoot.element("children", getCommentsThread(commentRoot, commentableDoc, new JSONArray()));
				commentsTree.add(jsonCommentRoot);
				if (StringUtils.isBlank(author)) {
					log.warn("Missing comment author on comment ID '" + commentRoot.getId() + "' (content: '" + commentRoot.getProperty(schemaPrefix, "text") + "')");
				}
			}
		}

		return createBlob(commentsTree);

	}

	private JSONArray getCommentsThread(DocumentModel comment, CommentableDocument commentableDocService,
			JSONArray threads) throws ClientException {
	    String schemaPrefix = getSchema(document.getType());
		List<DocumentModel> childrenComments = commentableDocService.getComments(comment);
		if (childrenComments == null || childrenComments.isEmpty()) {
			return threads;
		} else {
			for (DocumentModel childComment : childrenComments) {
				JSONObject jsonChildComment = new JSONObject();
				jsonChildComment.element("id", childComment.getId());
				String author = (String) childComment.getProperty(schemaPrefix, "author");
				jsonChildComment.element("author", author);
				jsonChildComment.element("creationDate", childComment.getProperty(schemaPrefix, "creationDate"));
				jsonChildComment.element("content", childComment.getProperty(schemaPrefix, "text"));
				jsonChildComment.element("modifiedDate", childComment.getProperty("dublincore", "modified"));
				boolean canDelete = canDeleteComment(author);
				jsonChildComment.element("canDelete", canDelete);
				jsonChildComment.element("children",
						getCommentsThread(childComment, commentableDocService, new JSONArray()));
				threads.add(jsonChildComment);

				if (StringUtils.isBlank(author)) {
					log.warn("Missing comment author on comment ID '" + childComment.getId() + "' (content: '" + childComment.getProperty(schemaPrefix, "text") + "')");
				}
			}
			return threads;
		}
	}

	private Blob createBlob(JSONArray json) {
		return new StringBlob(json.toString(), "application/json");
	}

	private boolean canDeleteComment(String author) {
		boolean canDelete = false;
		Principal user = session.getPrincipal();
		if (user != null) {
			boolean isUserAuthor = user.getName().equals(author);
			boolean isUserAdmin = ((NuxeoPrincipal) user).isAdministrator();
			canDelete = isUserAuthor || isUserAdmin;
		}
		return canDelete;
	}
	
	protected static String getSchema(String documentType){
	    String schemaPrefix = FetchCommentsOfDocument.COMMENT_SCHEMA;
        if(AddComment.THREAD_TYPE.equals(documentType)){
            schemaPrefix = FetchCommentsOfDocument.POST_SCHEMA;
        }
        return schemaPrefix;
	}

}
