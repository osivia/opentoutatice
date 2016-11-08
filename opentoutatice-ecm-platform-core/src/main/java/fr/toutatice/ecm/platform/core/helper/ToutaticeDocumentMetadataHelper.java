/**
 * 
 */
package fr.toutatice.ecm.platform.core.helper;

import org.nuxeo.ecm.core.api.CoreSession;


/**
 * @author david
 *
 */
public class ToutaticeDocumentMetadataHelper {
    
    public static final String UNICITY_TITLE_QUERY = "select * from Document where ecm:parentId = '%s' and "
            .concat("dc:title = '%s' and ecm:isProxy = 0 and ecm:currentLifeCycleState <> 'deleted' and ecm:isVersion = 0");
    protected static final String UNICITY_TITLE_EXCLUDE_ITSELF_CLAUSE = " and ecm:uuid <> '%s'";

    /**
     * Utility class.
     */
    public ToutaticeDocumentMetadataHelper() {
        super();
    }
    
    /**
     * Checks title unicity in current Folder.
     * 
     * @param value
     * @return true if title unicity in current Folder
     */
    public static boolean isTileUnique(CoreSession session, String parentUUId, String currentUUId, String title) {
            String query = String .format(UNICITY_TITLE_QUERY, parentUUId, title);
            if(currentUUId != null){
                query = String.format(query.concat(UNICITY_TITLE_EXCLUDE_ITSELF_CLAUSE), currentUUId);
            }
            return session.query(query).isEmpty();
    }

}
