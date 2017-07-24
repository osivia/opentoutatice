package fr.toutatice.ecm.platform.automation.comments;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.core.util.DocumentHelper;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;
import org.nuxeo.ecm.platform.comment.workflow.utils.CommentsConstants;
import org.nuxeo.ecm.platform.comment.workflow.utils.FollowTransitionUnrestricted;

/**
 * Comment operation abstract super-class.
 *
 * @author Cédric Krommenhoek
 */
public abstract class AbstractCommentOperation {

    /** Schema storing attached Blobs. */
    public static final String ATTACHED_BLOBS_SCHEMA = "files";
    /** Xpath storing attached Blobs. */
    public static final String ATTACHED_BLOBS_XPATH = "files:files";

    /**
     * Constructor.
     */
    public AbstractCommentOperation() {
        super();
    }


    /**
     * Execute operation.
     *
     * @param session core session
     * @param document document
     * @param parent parent comment, may be null
     * @param content comment content
     * @param author comment author, may be null
     * @param creationDate comment creation date, may be null
     * @param title thread post title
     * @param fileNames thread post files names
     * @param blob thread post file BLOB
     * @return comment
     */
    public DocumentModel execute(CoreSession session, DocumentModel document, DocumentModel parent, String content, String author, Date creationDate,
            String title, BlobList blobs) {
        // Commentable document
        CommentableDocument commentableDocument = document.getAdapter(CommentableDocument.class);

        // Comment type
        CommentType commentType = CommentType.fromParentType(document.getType());
        // Comment schema
        String schema = commentType.getSchema();


        // Author
        if (StringUtils.isEmpty(author)) {
            Principal principal = session.getPrincipal();
            if (principal == null) {
                throw new ClientException("No author for comment.");
            } else {
                author = principal.getName();
            }
        }

        // Creation date
        if (creationDate == null) {
            creationDate = new Date();
        }


        // Comment document
        DocumentModel comment = session.createDocumentModel(commentType.getType());
        comment.setProperty(schema, "text", content);
        comment.setProperty(schema, "author", author);
        comment.setProperty(schema, "creationDate", creationDate);

        if (CommentType.POST.equals(commentType)) {
            comment.setProperty(schema, "title", StringUtils.trimToEmpty(title));

            comment = setAttachments(comment, blobs);
        }

        if (parent == null) {
            comment = commentableDocument.addComment(comment);
        } else {
            comment = commentableDocument.addComment(parent, comment);
        }


        if (CommentType.POST.equals(commentType)) {
            Boolean moderated = (Boolean) document.getProperty(schema, "moderated");
            if (BooleanUtils.isNotTrue(moderated)) {
                FollowTransitionUnrestricted transition = new FollowTransitionUnrestricted(session, comment.getRef(),
                        CommentsConstants.TRANSITION_TO_PUBLISHED_STATE);
                transition.runUnrestricted();
            }
        }

        return comment;
    }

    /**
     * Sets Blobs to comment.
     * 
     * @param blobs
     */
    protected DocumentModel setAttachments(DocumentModel comment, BlobList blobs) {
        if (CollectionUtils.isNotEmpty(blobs)) {

            List<Map<String, Serializable>> existingBlobs = (List<Map<String, Serializable>>) comment.getPropertyValue(ATTACHED_BLOBS_XPATH);
            if (existingBlobs == null) {
                existingBlobs = new ArrayList<>();
            }
            for (Blob blob : blobs) {
                Map<String, Serializable> blobProp = DocumentHelper.createBlobHolderMap(blob);
                existingBlobs.add(blobProp);
            }
            comment.setPropertyValue(ATTACHED_BLOBS_XPATH, (Serializable) existingBlobs);
        }

        return comment;
    }

}
