package fr.toutatice.ecm.platform.service.fragments.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.fragments.FragmentServiceException;

/**
 * 
 * Service dédié aux fragments zoom
 * 
 */
public class ZoomFragment implements Fragment {

    private static final String ZOOM_SCHEMA = "zoom_fragments";
    private static final String ZOOM_LINKS_SCHEMA = "zoom_links";
	
	@Override
    public String prepareCreation(DocumentModel doc, String uri, String region,
 String belowUri, String code2) throws FragmentServiceException {

        Map<String, Object> links;
        Map<String, Object> frags;
		try {
            links = doc.getProperties(ZOOM_LINKS_SCHEMA);

            Collection<Object> values = links.values();

			// Une seule liste dans ce schéma
			Object liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

				Map<String, String> newEntry = new HashMap<String, String>();
				newEntry.put("refURI", uri);
                newEntry.put("order", "0");
                listeData.add(newEntry);

                Map<String, String> newEntry1 = new HashMap<String, String>();
                newEntry1.put("refURI", uri);
                newEntry1.put("order", "1");
                listeData.add(newEntry1);

                Map<String, String> newEntry2 = new HashMap<String, String>();
                newEntry2.put("refURI", uri);
                newEntry2.put("order", "2");
                listeData.add(newEntry2);

                Map<String, String> newEntry3 = new HashMap<String, String>();
                newEntry3.put("refURI", uri);
                newEntry3.put("order", "3");
                listeData.add(newEntry3);

                Map<String, String> newEntry4 = new HashMap<String, String>();
                newEntry4.put("refURI", uri);
                newEntry4.put("order", "4");
                listeData.add(newEntry4);

                doc.setProperties(ZOOM_LINKS_SCHEMA, links);
			}


            frags = doc.getProperties(ZOOM_SCHEMA);

            values = frags.values();

            // Une seule liste dans ce schéma
            liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

                Map<String, String> newEntry = new HashMap<String, String>();
                newEntry.put("refURI", uri);
                listeData.add(newEntry);

                doc.setProperties(ZOOM_SCHEMA, frags);
            }

		} catch (ClientException e) {
            throw new FragmentServiceException(e);
		}
		return uri;
	}

}
