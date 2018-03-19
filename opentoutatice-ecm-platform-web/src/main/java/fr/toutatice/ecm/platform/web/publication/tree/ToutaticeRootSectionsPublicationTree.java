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
 * 
 */
package fr.toutatice.ecm.platform.web.publication.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.impl.core.RootSectionsPublicationTree;
import org.nuxeo.ecm.platform.publisher.impl.core.VirtualCoreFolderPublicationNode;


/**
 * @author david
 * 
 */
public class ToutaticeRootSectionsPublicationTree extends RootSectionsPublicationTree {

    private static final long serialVersionUID = 6953076925201018520L;

    /** Logger. */
    private static final Log log = LogFactory.getLog(ToutaticeRootSectionsPublicationTree.class);

    public static final String SECTION_ROOT_TYPE = "SectionRoot";

    /* FIXME: Fork to use ToutaticeCoreFolderPublicationNode... and rootNode.getChildrenNodes() */
    @Override
    public List<PublicationNode> getChildrenNodes() throws NuxeoException {
        if (currentDocument != null && useRootSections) {
            // FIXME: In fact, rootFinder.getAccessibleSectionRoots returns Roots ans children
            // We work only with workspace configuration
            DocumentModelList rootSections = rootFinder.getAccessibleSectionRoots(currentDocument);

            List<PublicationNode> publicationNodes = new ArrayList<PublicationNode>();
            for (DocumentModel rootSection : rootSections) {
                if (isPublicationNode(rootSection)) {
                    publicationNodes.add(new ToutaticeCoreFolderPublicationNode(rootSection, getConfigName(), sid, rootNode, factory));
                }
            }
            return publicationNodes;
        }
        return super.getChildrenNodes();
    }

    /* FIXME: Fork to use getToutaticeNodeByPath and getToutaticeFreeNodeByPath */
    @Override
    public PublicationNode getNodeByPath(String path) throws NuxeoException {
        // if we ask for the root path of this tree, returns this because
        // of the custom implementations of some methods (getChildrenNodes)
        if (path.equals(rootPath)) {
            return this;
        } else {
            // if we ask for a section root, returns a correct PublicationNode
            // (with parent set to this tree)
            List<PublicationNode> children = getChildrenNodes();
            for (PublicationNode child : children) {
                if (child.getPath().equals(path)) {
                    return child;
                }
            }
            return getToutaticeNodeByPath(path);
        }
    }

    /* FIXME: Fork of SectionPublicationTree method to use ToutaticeCoreFolderPublicationNode */
    public PublicationNode getToutaticeNodeByPath(String path) throws NuxeoException {
        DocumentRef docRef = new PathRef(path);
        if (coreSession.hasPermission(docRef, SecurityConstants.READ)) {
            return new ToutaticeCoreFolderPublicationNode(coreSession.getDocument(new PathRef(path)), getConfigName(), getSessionId(), factory);
        } else {
            return new VirtualCoreFolderPublicationNode(coreSession.getSessionId(), path, getConfigName(), sid, factory);
        }
    }

    /* FIXME: Fork to exclude SectionRoot publication */
    @Override
    public boolean canPublishTo(PublicationNode publicationNode) throws NuxeoException {
        if (publicationNode == null || publicationNode.getParent() == null) {
            // we can't publish in the root node
            return false;
        }
        DocumentRef docRef = new PathRef(publicationNode.getPath());
        boolean canAsk = coreSession.hasPermission(docRef, CAN_ASK_FOR_PUBLISHING);

        DocumentModel document = coreSession.getDocument(docRef);
        boolean notSectionRoot = !SECTION_ROOT_TYPE.equals(document.getType());

        return canAsk && notSectionRoot;
    }

}
