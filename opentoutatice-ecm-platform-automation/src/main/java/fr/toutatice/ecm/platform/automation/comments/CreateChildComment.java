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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;

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
    public Object run() throws Exception {
        CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
        DocumentModel childComment = AddComment.createComment(document.getRef(), document.getType(), session, childCommentContent, childCommentTitle, fileName);
        DocumentModel commentDoc = commentableDoc.addComment(comment, childComment);
        
        UnrestrictedAclAndLifeCycleCommentRunner runner = new UnrestrictedAclAndLifeCycleCommentRunner(session, document, commentDoc);
        runner.runUnrestricted();
        commentDoc = runner.getCommentCreated();
        return new StringBlob(commentDoc.getId());
    }
    
    /* 
     * The new comment dosen't have user ACL, so we must follow transition in unrestricted mode.
     * We decide to do all creation in unrestricted mode.
     */
    private static class UnrestrictedAclAndLifeCycleCommentRunner extends UnrestrictedSessionRunner {
        
        private DocumentModel document;
        private DocumentModel commentCreated;
        
        public DocumentModel getCommentCreated() {
            return this.commentCreated;
        }

        protected UnrestrictedAclAndLifeCycleCommentRunner(CoreSession session, DocumentModel document, DocumentModel commentCreated) {
            super(session);
            this.document = document;
            this.commentCreated = commentCreated;
        }


        @Override
        public void run() throws ClientException {
            boolean isPost = AddComment.THREAD_TYPE.equals(this.document.getType());
            ACP acp = this.session.getACP(this.document.getRef());
            this.session.setACP(this.commentCreated.getRef(), acp, true);
            if (isPost) {
                Boolean isModerated = (Boolean) this.document.getProperty("thread", "moderated");
                if (!isModerated) {
                    this.session.followTransition(this.commentCreated.getRef(), AddComment.PUBLISHED_TRANSITION);
                }
            }
        }
        
    }

}
