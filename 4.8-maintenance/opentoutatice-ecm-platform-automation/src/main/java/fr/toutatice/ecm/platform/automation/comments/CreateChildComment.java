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

import java.util.Date;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Create child comment operation.
 */
@Operation(id = CreateChildComment.ID, category = Constants.CAT_DOCUMENT, label = "CreateChildCommentOfDocument",
        description = "Add a child comment (answer) to a (commentable) document")
public class CreateChildComment extends AbstractCommentOperation {

    /** Operation identifier. */
    public static final String ID = "Document.CreateChildComment";


    /** Core session. */
    @Context
    private CoreSession session;

    /** Commentable document parameter. */
    @Param(name = "document")
    private DocumentModel document;

    /** Parent comment parameter. */
    @Param(name = "parent")
    private DocumentModel parent;

    /** Comment content parameter. */
    @Param(name = "content")
    private String content;

    /** Comment author parameter. */
    @Param(name = "author", required = false)
    private String author;

    /** Comment creation date parameter. */
    @Param(name = "creationDate", required = false)
    private Date creationDate;

    /** Thread post title parameter. */
    @Param(name = "title", required = false)
    private String title;

    /**
     * Constructor.
     */
    public CreateChildComment() {
        super();
    }


    /**
     * Run operation.
     *
     * @return comment
     */
    @OperationMethod
    public DocumentModel run() {
        return this.execute(this.session, this.document, this.parent, this.content, this.author, this.creationDate, this.title, null);
    }


    /**
     * Run operation.
     *
     * @param blob thread post file BLOB
     * @return comment
     */
    @OperationMethod
    public DocumentModel run(BlobList blobs) {
        return this.execute(this.session, this.document, this.parent, this.content, this.author, this.creationDate, this.title, blobs);
    }

}
