/**
 * 
 */
package fr.toutatice.ecm.platform.web.publication.tree;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.schema.FacetNames;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocumentFactory;
import org.nuxeo.ecm.platform.publisher.impl.core.CoreFolderPublicationNode;


/**
 * @author David Chevrier
 * 
 */
public class ToutaticeCoreFolderPublicationNode extends CoreFolderPublicationNode {

    private static final long serialVersionUID = 8111887640605954307L;

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
        clause.append(" AND ecm:isProxy = 0 ");

        return clause.toString();
    }

}
