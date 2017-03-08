/**
 * 
 */
package fr.toutatice.ecm.platform.services.permalink;

import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david
 *
 */
public class PermalinkByWebId implements Permalink {
    
    /** Path separator. */
    private static final String SEPARATOR = "/";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPermalink(DocumentModel document, String host, Map<String, String> params) {
        // Host
        StringBuilder permalinkResult = new StringBuilder()
        .append(host).append(SEPARATOR);
        // Segments URL
        for (String param : params.values()) {
            permalinkResult.append(param).append(SEPARATOR);
        }

        // WebId
        String webId = (String) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
        
        if(webId == null){
            throw new NullPointerException(String.format("The webid is null. Document's id='%s'", document.getId()));
        } 
        
        permalinkResult.append(webId);
        return permalinkResult.toString();      
    }

}
