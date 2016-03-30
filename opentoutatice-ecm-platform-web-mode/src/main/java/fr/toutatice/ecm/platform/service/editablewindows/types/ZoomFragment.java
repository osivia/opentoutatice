package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;

/**
 * 
 * Service dédié aux fragments zoom
 * 
 */
public class ZoomFragment implements EditableWindow {

    private static final String ZOOM_SCHEMA = "zoom_fragments";
    private static final String ZOOM_LINKS_SCHEMA = "zoom_links";
	
	@Override
    public String prepareCreation(DocumentModel doc, String uri, String region,
 String belowUri, String code2) throws EwServiceException {

        Map<String, Object> links;
        Map<String, Object> frags;
		try {


            frags = doc.getProperties(ZOOM_SCHEMA);

            Collection<Object>  values = frags.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

                Map<String, String> newEntry = new HashMap<String, String>();
                newEntry.put("refURI", uri);
                listeData.add(newEntry);

                doc.setProperties(ZOOM_SCHEMA, frags);
            }

		} catch (ClientException e) {
            throw new EwServiceException(e);
		}
		return uri;
	}

}
