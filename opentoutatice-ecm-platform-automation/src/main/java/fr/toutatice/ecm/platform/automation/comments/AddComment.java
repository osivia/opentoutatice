/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.automation.comments;

import java.security.Principal;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

@Operation(id = AddComment.ID, category = Constants.CAT_DOCUMENT, label = "AddCommentToDocument", description = "Add a comment to a (commentable) document")
public class AddComment {

    public static final String ID = "Document.AddComment";

    public static final String COMMENT_TYPE = "Comment";
    public static final String THREAD_TYPE = "Thread";
    public static final String POST_TYPE = "Post";

    public static final String PUBLISHED_TRANSITION = "moderation_publish";

    @Context
    protected CoreSession session;

    @Param(name = "commentableDoc", required = true)
    protected DocumentModel document;

    @Param(name = "comment", required = true)
    protected String commentContent;

    @Param(name = "title", required = false)
    protected String commentTitle;

    @Param(name = "fileName", required = false)
    protected String fileName;

    @OperationMethod
    public Object run(Blob file) throws Exception {

        CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
        DocumentModel comment = createComment(document.getRef(), document.getType(), session, commentContent, commentTitle, fileName, file);
        DocumentModel commentDoc = commentableDoc.addComment(comment);
        if(THREAD_TYPE.equals(document.getType())){
            Boolean isModerated = (Boolean) document.getProperty("thread", "moderated");
            if(!isModerated){
                session.followTransition(commentDoc.getRef(), PUBLISHED_TRANSITION);
            }
        }
        return document;

    }

    public static DocumentModel createComment(DocumentRef commentRef, String docType, CoreSession session, String commentContent, String commentTitle,
            String fileName, Blob file)
            throws ClientException {
        String commentType = getType(docType);
        String schemaPrefix = FetchCommentsOfDocument.getSchema(docType);

        DocumentModel comment = session.createDocumentModel(commentType);
        Principal user = session.getPrincipal();
        if (user == null) {
            throw new ClientException("No author for comment.");
        }
        comment.setProperty(schemaPrefix, "author", user.getName());
        comment.setProperty(schemaPrefix, "text", commentContent);
        comment.setProperty(schemaPrefix, "creationDate", Calendar.getInstance());
        if (POST_TYPE.equals(commentType)) {
            comment.setProperty(schemaPrefix, "title", commentTitle);
            if(StringUtils.isNotEmpty(fileName) && file != null){
                comment.setProperty(FetchCommentsOfDocument.POST_SCHEMA, "filename", fileName);
                comment.setProperty(FetchCommentsOfDocument.POST_SCHEMA, "fileContent", file);
            }
        }
        return comment;
    }

    protected static String getType(String documentType) {
        String type = COMMENT_TYPE;
        if (AddComment.THREAD_TYPE.equals(documentType)) {
            type = POST_TYPE;
        }
        return type;
    }

}
