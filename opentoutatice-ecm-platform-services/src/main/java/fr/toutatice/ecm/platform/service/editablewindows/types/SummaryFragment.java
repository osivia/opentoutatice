/**
 * 
 */
package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;


/**
 * @author David Chevrier
 *
 */
public class SummaryFragment implements EditableWindow {
    
    private static final String SUMMARY_SCHEMA = "summary_fragments";

    @Override
    public String prepareCreation(DocumentModel doc, String uri, String region, String belowUri, String code2) throws EwServiceException {
        Map<String, Object> properties;
        try {
            properties = doc.getProperties(SUMMARY_SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

                Map<String, String> newEntry = new HashMap<String, String>();

                newEntry.put("refURI", uri);
                /* FIXME: default value */
                newEntry.put("regionId", "contenu");

                listeData.add(newEntry);

                doc.setProperties(SUMMARY_SCHEMA, properties);
            }

        } catch (ClientException e) {
            throw new EwServiceException(e);
        }
        return uri;
    }

}
