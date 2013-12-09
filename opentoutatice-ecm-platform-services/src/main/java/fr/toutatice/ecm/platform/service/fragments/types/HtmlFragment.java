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
 * Service dédié aux fragments html
 *
 */
public class HtmlFragment implements Fragment {

	private static final String HTML_SCHEMA = "html_fragments";

	
	@Override
    public String prepareCreation(DocumentModel doc, String uri, String region,
 String belowUri, String code2) throws FragmentServiceException {

		Map<String, Object> properties;
		try {
			properties = doc.getProperties(HTML_SCHEMA);

			Collection<Object> values = properties.values();

			// Une seule liste dans ce schéma
			Object liste = values.iterator().next();

			if (liste instanceof List) {
				List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

				Map<String, String> newEntry = new HashMap<String, String>();

				newEntry.put("refURI", uri);
				newEntry.put("data", "Nouveau contenu");

				listeData.add(newEntry);

				doc.setProperties(HTML_SCHEMA, properties);
			}

		} catch (ClientException e) {
            throw new FragmentServiceException(e);
		}
		return uri;
	}

}
