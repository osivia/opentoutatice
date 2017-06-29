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
package fr.toutatice.ecm.platform.web.publication.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.tree.DefaultDocumentTreeSorter;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocumentFactory;
import org.nuxeo.ecm.platform.publisher.impl.core.CoreFolderPublicationNode;

import fr.toutatice.ecm.platform.core.helper.ToutaticeEsQueryHelper;


/**
 * @author David Chevrier
 * Used to override buildChildrenWhereClause query.
 */
public class ToutaticeCoreFolderPublicationNode extends CoreFolderPublicationNode {

    private static final long serialVersionUID = 8111887640605954307L;
    
    /** Logger. */
    private static final Log log = LogFactory.getLog(ToutaticeCoreFolderPublicationNode.class);

    public ToutaticeCoreFolderPublicationNode(DocumentModel doc, String treeConfigName, String sid, PublicationNode parent, PublishedDocumentFactory factory)
            throws ClientException {
        super(doc, treeConfigName, sid, parent, factory);
    }

    public ToutaticeCoreFolderPublicationNode(DocumentModel document, String configName, String sessionId, PublishedDocumentFactory factory) throws ClientException {
        super(document, configName, sessionId, factory);
    }

    @Override
    protected String buildChildrenWhereClause(boolean queryDocuments) {
        StringBuffer clause = new StringBuffer();
        clause.append(String.format("ecm:parentId = '%s' AND ecm:currentLifeCycleState != '%s'", folder.getId(), LifeCycleConstants.DELETED_STATE));
        clause.append(String.format(" AND ecm:mixinType IN ('%s' , '%s' ) ", FacetNames.MASTER_PUBLISH_SPACE, FacetNames.PUBLISH_SPACE));
        clause.append(String.format(" AND ecm:mixinType NOT IN ('%s')", FacetNames.HIDDEN_IN_NAVIGATION));
//        clause.append(String.format(" AND ecm:primaryType NOT IN ('%s')", SECTION_ROOT_TYPE));
        clause.append(" AND ecm:isProxy = 0 ");

        return clause.toString();
    }
    
    /* Fork to use ToutaticeCoreFolderPublicationNode ... */
    @Override
    public List<PublicationNode> getChildrenNodes() throws ClientException {
        DocumentModelList children = getSortedChildren(false);

        List<PublicationNode> childrenNodes = new ArrayList<PublicationNode>();
        for (DocumentModel child : children) {
            childrenNodes.add(new ToutaticeCoreFolderPublicationNode(child,
                    treeConfigName, sid, this, factory));
        }
        return childrenNodes;
    }

    // Fork to querying with ES
    @Override
    protected DocumentModelList getSortedChildren(boolean queryDocuments) throws ClientException {
        String whereClause = buildChildrenWhereClause(queryDocuments);

        DocumentModelList children = ToutaticeEsQueryHelper.query(getCoreSession(), "SELECT * FROM Document WHERE " + whereClause);

        if (!folder.hasFacet(FacetNames.ORDERABLE)) {
            DefaultDocumentTreeSorter sorter = new DefaultDocumentTreeSorter();
            sorter.setSortPropertyPath("dc:title");
            Collections.sort(children, sorter);
        }
        return children;
    }

}
