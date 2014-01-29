
package fr.toutatice.ecm.platform.service.permalink;

import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;


public class PermalinkByPathDoc implements Permalink
{

	private static final String SEPARATOR = "/";
    

	@Override
    public String getPermalink(DocumentModel doc, String host, Map<String,String> params)
    {
        StringBuilder permalinkResult = new StringBuilder();
        permalinkResult.append(host);
        permalinkResult.append(SEPARATOR);
       for (String param : params.values()) {
    	   permalinkResult.append(param);
    	   permalinkResult.append(SEPARATOR);
       } 
       
       // recherche du path du doc
        String docPath = doc.getPath().toString();
        if(docPath == null){
            throw new NullPointerException(String.format("The document path is null. Document's id='%s'", doc.getId()));
        } else if(docPath.startsWith(SEPARATOR)){
        	docPath = docPath.substring(1);
        }
        permalinkResult.append(docPath);
        
        return permalinkResult.toString();      
    }

    
}
