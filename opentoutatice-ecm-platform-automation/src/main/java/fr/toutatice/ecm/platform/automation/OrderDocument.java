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
 * dchevrier
 */
package fr.toutatice.ecm.platform.automation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelIterator;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.elasticsearch.api.ElasticSearchIndexing;
import org.nuxeo.elasticsearch.commands.IndexingCommand;
import org.nuxeo.runtime.api.Framework;


/**
 * @author David Chevrier.
 *
 */
@Operation(id = OrderDocument.ID, category = Constants.CAT_DOCUMENT, label = "Order input Documents",
        description = "Order the given documents in the given container. The orderd document will become the input of the next operation."
                + "Manage associated local proxies if any.")
public class OrderDocument {

    public static final String ID = "Document.OrderDocument";

    @Context
    protected CoreSession session;

    @Param(name = "sourceId", required = true)
    protected DocumentModel source;

    @Param(name = "targetId", required = false)
    protected String targetId = StringUtils.EMPTY;

    @Param(name = "position", values = {"before", "after"}, required = false)
    protected String position = "before";

    @OperationMethod
    public DocumentModel run() throws Exception {
        DocumentModel parentDocument = session.getParentDocument(source.getRef());

        DocumentModel target = getTargetDocument();
        String targetName = null;
        if (target != null) {
            targetName = target.getName();
        }

        if (StringUtils.equalsIgnoreCase("before", this.position)) {

            this.session.orderBefore(parentDocument.getRef(), source.getName(), targetName);
            orderProxyBefore(parentDocument, targetName);

        } else if (StringUtils.equalsIgnoreCase("after", position)) {

            this.session.orderBefore(parentDocument.getRef(), targetName, source.getName());
            orderProxyAfter(parentDocument, targetName);

        }

        // Explicit indexing of reordered children
        indexReorderedChildren(parentDocument);

        return source;

    }


    /**
     * Index synchronously reordered children.
     * 
     * @param parentDocument
     */
    public void indexReorderedChildren(DocumentModel parentDocument) {
        // Filter on trashed and version children
        Filter filter = new Filter() {

            private static final long serialVersionUID = 6829091503899474742L;

            @Override
            public boolean accept(DocumentModel docModel) {
                boolean notTrashed = !StringUtils.equals(LifeCycleConstants.DELETED_STATE, docModel.getCurrentLifeCycleState());
                return !docModel.isVersion() && notTrashed;
            }
        };

        // Direct children
        DocumentModelIterator childrenIterator = this.session.getChildrenIterator(parentDocument.getRef(), null, null, filter);
        if (childrenIterator != null) {
            List<IndexingCommand> cmds = new ArrayList<>(2);
            // Commands
            while (childrenIterator.hasNext()) {
                cmds.add(new IndexingCommand(childrenIterator.next(), IndexingCommand.Type.UPDATE, true, false));
            }

            // Synchronous indexing
            ElasticSearchIndexing esi = (ElasticSearchIndexing) Framework.getService(ElasticSearchIndexing.class);
            esi.indexNonRecursive(cmds);
        }
    }


    /**
     * @return the target DocumentModel given its id or path.
     */
    private DocumentModel getTargetDocument() {
        DocumentModel target = null;
        if (StringUtils.isNotBlank(targetId)) {
            try {
                target = session.getDocument(new IdRef(targetId));
            } catch (Exception ie) {
                try {
                    target = session.getDocument(new PathRef(targetId));
                } catch (Exception pe) {
                    target = null;
                }
            }
        }
        return target;
    }

    /**
     * @param parentDocument
     * @param target
     */
    private void orderProxyBefore(DocumentModel parentDocument, String targetName) {
        // Case of local proxies
        DocumentModelList localProxies = session.getProxies(source.getRef(), parentDocument.getRef());
        if (CollectionUtils.isNotEmpty(localProxies)) {
            for (DocumentModel proxy : localProxies) {
                this.session.orderBefore(parentDocument.getRef(), proxy.getName(), targetName);
            }
        }
    }

    /**
     * @param parentDocument
     * @param target
     */
    private void orderProxyAfter(DocumentModel parentDocument, String targetName) {
        // Case of local proxies
        DocumentModelList localProxies = session.getProxies(source.getRef(), parentDocument.getRef());
        if (CollectionUtils.isNotEmpty(localProxies)) {
            for (DocumentModel proxy : localProxies) {
                this.session.orderBefore(parentDocument.getRef(), targetName, proxy.getName());
            }
        }
    }

}
