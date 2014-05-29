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
 * mberhaut1
 */
package fr.toutatice.ecm.platform.automation.comments;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;
import org.nuxeo.ecm.platform.comment.workflow.utils.CommentsConstants;
import org.nuxeo.ecm.platform.comment.workflow.utils.FollowTransitionUnrestricted;

@Operation(id = CreateChildComment.ID, category = Constants.CAT_DOCUMENT, label = "CreateChildCommentOfDocument",
        description = "Add a child comment (answer) to a (commentable) document")
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

    @Param(name = "childCommentTitle", required = false)
    protected String childCommentTitle;

    @Param(name = "fileName", required = false)
    protected String fileName;

    @OperationMethod
    public Object run(Blob blob) throws Exception {
        CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
        DocumentModel childComment = AddComment.createComment(document.getType(), session, childCommentContent, childCommentTitle, fileName, blob);
        DocumentModel commentDoc = commentableDoc.addComment(comment, childComment);

        boolean isPost = AddComment.THREAD_TYPE.equals(this.document.getType());
        if (isPost) {
            Boolean isModerated = (Boolean) this.document.getProperty("thread", "moderated");
            if (!isModerated) {
                FollowTransitionUnrestricted transition = new FollowTransitionUnrestricted(session, commentDoc.getRef(), CommentsConstants.TRANSITION_TO_PUBLISHED_STATE);
                transition.runUnrestricted();
            }
        }

         return new StringBlob(commentDoc.getId());
    }

}
