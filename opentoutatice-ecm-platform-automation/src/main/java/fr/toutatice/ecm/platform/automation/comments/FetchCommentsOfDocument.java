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

import java.io.IOException;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.comment.api.CommentableDocument;
import org.nuxeo.ecm.platform.dublincore.NXDublinCore;
import org.nuxeo.ecm.platform.dublincore.service.DublinCoreStorageService;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

@Operation(id = FetchCommentsOfDocument.ID, category = Constants.CAT_FETCH, label = "FetchCommentsOfDocument",
        description = "Fetches comments of a (commentable) document")
public class FetchCommentsOfDocument {

    public static final String ID = "Fetch.DocumentComments";

    private static final Log log = LogFactory.getLog(FetchCommentsOfDocument.class);

    public static final String COMMENT_SCHEMA = "comment";
    public static final String POST_SCHEMA = "post";

    /** Total number of root comments. */
    private int nbRootsComments;
    /** Toatal number of comments (children of roots). */
    private int nbChildrenComments;

    @Context
    CoreSession session;

    @Param(name = "commentableDoc", required = true)
    protected DocumentModel document;

    @OperationMethod
    public Object run() throws ClientException, IOException {

        JSONArray commentsTree = new JSONArray();
        /*
         * Récupération du service de commentaires.
         */
        CommentableDocument commentableDoc = document.getAdapter(CommentableDocument.class);
        String schemaPrefix = "comment";
        if (AddComment.THREAD_TYPE.equals(document.getType())) {
            schemaPrefix = "post";
        }
        List<DocumentModel> commentsRoots = commentableDoc.getComments();
        if (commentsRoots != null) {
            nbRootsComments = commentsRoots.size();
            /*
             * Construction de la liste des fils de commentaires.
             */
            for (DocumentModel commentRoot : commentsRoots) {
                JSONObject jsonCommentRoot = new JSONObject();
                jsonCommentRoot.element("id", commentRoot.getId());
                jsonCommentRoot.element("path", commentRoot.getPathAsString());
                String author = (String) commentRoot.getProperty(schemaPrefix, "author");
                jsonCommentRoot.element("author", author);
                jsonCommentRoot.element("creationDate", commentRoot.getProperty(schemaPrefix, "creationDate"));
                jsonCommentRoot.element("content", commentRoot.getProperty(schemaPrefix, "text"));
                jsonCommentRoot.element("modifiedDate", commentRoot.getProperty("dublincore", "modified"));
                boolean canDelete = canDeleteComment(author, document);
                jsonCommentRoot.element("canDelete", canDelete);
                if (AddComment.THREAD_TYPE.equals(document.getType())) {
                    jsonCommentRoot.element("title", commentRoot.getProperty(schemaPrefix, "title"));
                    jsonCommentRoot.element("filename", commentRoot.getProperty(schemaPrefix, "filename"));
                }
                jsonCommentRoot.element("children", getCommentsThread(commentRoot, commentableDoc, new JSONArray()));
                commentsTree.add(jsonCommentRoot);
                if (StringUtils.isBlank(author)) {
                    log.warn("Missing comment author on comment ID '" + commentRoot.getId() + "' (content: '" + commentRoot.getProperty(schemaPrefix, "text")
                            + "')");
                }
            }
        }

        updateDocument();

        return createBlob(commentsTree);

    }

    private JSONArray getCommentsThread(DocumentModel comment, CommentableDocument commentableDocService, JSONArray threads) throws ClientException {
        String schemaPrefix = getSchema(document.getType());
        List<DocumentModel> childrenComments = commentableDocService.getComments(comment);
        if (childrenComments != null) {
            nbChildrenComments += childrenComments.size();
        }
        if (childrenComments == null || childrenComments.isEmpty()) {
            return threads;
        } else {
            for (DocumentModel childComment : childrenComments) {
                JSONObject jsonChildComment = new JSONObject();
                jsonChildComment.element("id", childComment.getId());
                jsonChildComment.element("path", childComment.getPathAsString());
                String author = (String) childComment.getProperty(schemaPrefix, "author");
                jsonChildComment.element("author", author);
                jsonChildComment.element("creationDate", childComment.getProperty(schemaPrefix, "creationDate"));
                jsonChildComment.element("content", childComment.getProperty(schemaPrefix, "text"));
                jsonChildComment.element("modifiedDate", childComment.getProperty("dublincore", "modified"));
                boolean canDelete = canDeleteComment(author, document);
                jsonChildComment.element("canDelete", canDelete);
                if (AddComment.THREAD_TYPE.equals(document.getType())) {
                    jsonChildComment.element("title", childComment.getProperty(schemaPrefix, "title"));
                    jsonChildComment.element("filename", childComment.getProperty(schemaPrefix, "filename"));
                }
                jsonChildComment.element("children", getCommentsThread(childComment, commentableDocService, new JSONArray()));
                threads.add(jsonChildComment);

                if (StringUtils.isBlank(author)) {
                    log.warn("Missing comment author on comment ID '" + childComment.getId() + "' (content: '" + childComment.getProperty(schemaPrefix, "text")
                            + "')");
                }
            }
            return threads;
        }
    }

    /**
     * Update comments number on document if necessary.
     */
    // DCH: FIXME: temporary wainting better Forum model.
    private void updateDocument() {

        if (this.document.hasSchema("thread_toutatice")) {

            Long nbComments = (Long) this.document.getPropertyValue("ttcth:nbComments");
            Long newNbComments = Long.valueOf(this.nbRootsComments + this.nbChildrenComments);

            if (!nbComments.equals(newNbComments)) {
                // Update of Thread to be correctly ordered
                
                this.document.setPropertyValue("ttcth:nbComments", newNbComments);
                // Done in unrestricted way because a user can add post
                // even if he has no write permission on Thread
                ToutaticeDocumentHelper.saveDocumentSilently(this.session, this.document, true);
                
                DublinCoreStorageService service = NXDublinCore.getDublinCoreStorageService();
                
                Calendar modificationDate = Calendar.getInstance();
                modificationDate.setTime(new Date());
                
                service.setModificationDate(this.document, modificationDate, null);
                
                // "Virtual" event to pass principal to method
                DocumentEventContext ctx = new DocumentEventContext(session, this.session.getPrincipal(),
                        this.document);
                service.addContributor(this.document, ctx.newEvent(StringUtils.EMPTY));
            }

        }
    }

    private Blob createBlob(JSONArray json) {
        return new StringBlob(json.toString(), "application/json");
    }

    private boolean canDeleteComment(String author, DocumentModel document) {
        boolean canDelete = false;
        Principal user = session.getPrincipal();
        if (user != null) {
            boolean isUserAuthor = user.getName().equals(author);
            boolean isUserAdmin = ((NuxeoPrincipal) user).isAdministrator();
            boolean userHasAllRights = session.hasPermission(document.getRef(), SecurityConstants.EVERYTHING);
            canDelete = isUserAuthor || isUserAdmin || userHasAllRights;
        }
        return canDelete;
    }

    protected static String getSchema(String documentType) {
        String schemaPrefix = FetchCommentsOfDocument.COMMENT_SCHEMA;
        if (AddComment.THREAD_TYPE.equals(documentType)) {
            schemaPrefix = FetchCommentsOfDocument.POST_SCHEMA;
        }
        return schemaPrefix;
    }

}
