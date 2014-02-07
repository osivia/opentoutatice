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
    
    /* FIXME: fork... */
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

}
