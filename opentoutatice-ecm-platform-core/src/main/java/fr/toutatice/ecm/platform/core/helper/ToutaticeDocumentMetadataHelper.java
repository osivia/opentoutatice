/**
 * 
 */
package fr.toutatice.ecm.platform.core.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;



/**
 * @author david
 *
 */
public class ToutaticeDocumentMetadataHelper {
    
    public static final String UNICITY_TITLE_QUERY = "select * from Document where ecm:parentId = '%s' and "
            .concat("dc:title = '%s' and ecm:isProxy = 0 and ecm:currentLifeCycleState <> 'deleted' and ecm:isVersion = 0");
    protected static final String UNICITY_TITLE_EXCLUDE_ITSELF_CLAUSE = " and ecm:uuid <> '%s'";

    
    private static String[] allowedTypesWithSameTitle = {"Agenda"};
    
    
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
    	
    	
        title = StringUtils.replace(title, "'", "\\'");
        String query = String.format(UNICITY_TITLE_QUERY, parentUUId, title);
        if (currentUUId != null) {
            query = String.format(query.concat(UNICITY_TITLE_EXCLUDE_ITSELF_CLAUSE), currentUUId);
        }

        
		ElasticSearchService service = Framework.getService(ElasticSearchService.class);

		if(service !=null) {
			NxQueryBuilder queryBuilder = new NxQueryBuilder(session);
			queryBuilder.nxql(query);
			DocumentModelList results = service.query(queryBuilder);
			
			return results.isEmpty();
			
		}
		else {
	        return session.query(query).isEmpty();
		}
		

    }
    
    /**
     * Some types like agenda events can share the same title and be distingued by their dates 
     * 
     * @param type
     * @return
     */
    public static boolean isSameTitleAllowed(String type) {
    	for(String allowedType : allowedTypesWithSameTitle) {
    		if(type.equalsIgnoreCase(allowedType)) {
    			return true;
    		}
    	}
    	return false;
    }

}
