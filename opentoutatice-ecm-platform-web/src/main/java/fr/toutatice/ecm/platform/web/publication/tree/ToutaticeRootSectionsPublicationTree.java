/**
 * 
 */
package fr.toutatice.ecm.platform.web.publication.tree;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.impl.core.CoreFolderPublicationNode;
import org.nuxeo.ecm.platform.publisher.impl.core.RootSectionsPublicationTree;
import org.nuxeo.ecm.platform.publisher.impl.core.VirtualCoreFolderPublicationNode;


/**
 * @author david
 * 
 */
public class ToutaticeRootSectionsPublicationTree extends RootSectionsPublicationTree {

    private static final long serialVersionUID = 6953076925201018520L;
    
    /* FIXME: Fork to use ToutaticeCoreFolderPublicationNode */
    @Override
    public List<PublicationNode> getChildrenNodes() throws ClientException {
        if (currentDocument != null && useRootSections) {
            DocumentModelList rootSections = rootFinder.getAccessibleSectionRoots(currentDocument);
            if (rootSections.isEmpty()) {
                useRootSections = false;
                return super.getChildrenNodes();
            }
            List<PublicationNode> publicationNodes = new ArrayList<PublicationNode>();
            for (DocumentModel rootSection : rootSections) {
                if (isPublicationNode(rootSection)) {
                    publicationNodes.add(new ToutaticeCoreFolderPublicationNode(rootSection,
                            getConfigName(), sid, rootNode, factory));
                }
            }
            return publicationNodes;
        }
        return super.getChildrenNodes();
    }
    
    /* FIXME: Fork to use getToutaticeNodeByPath */
    @Override
    public PublicationNode getNodeByPath(String path) throws ClientException {
        if (!useRootSections) {
            return super.getNodeByPath(path);
        }
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
    
    /* FIXME: Fork of SectionPublicationTree méthod to use ToutaticeCoreFolderPublicationNode */
    public PublicationNode getToutaticeNodeByPath(String path) throws ClientException {
        DocumentRef docRef = new PathRef(path);
        if (coreSession.hasPermission(docRef, SecurityConstants.READ)) {
            return new ToutaticeCoreFolderPublicationNode(
                    coreSession.getDocument(new PathRef(path)),
                    getConfigName(), getSessionId(), factory);
        } else {
            return new VirtualCoreFolderPublicationNode(
                    coreSession.getSessionId(), path, getConfigName(), sid,
                    factory);
        }

    }

}
