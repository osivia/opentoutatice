package fr.toutatice.ecm.platform.service.editablewindows.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.editablewindows.EwServiceException;


public class SetFragment implements EditableWindow {

    private static final String SET_SCHEMA = "set_fragments";

    @Override
    public String prepareCreation(DocumentModel doc, String uri, String region, String belowUri, String code2) throws EwServiceException {
        Map<String, Object> properties;
        try {
            properties = doc.getProperties(SET_SCHEMA);

            Collection<Object> values = properties.values();

            // Une seule liste dans ce schéma
            Object liste = values.iterator().next();

            if (liste instanceof List) {
                List<Map<String, String>> listeData = (List<Map<String, String>>) liste;

                Map<String, String> newEntry = new HashMap<String, String>();

                newEntry.put("refURI", uri);
                newEntry.put("style", "normal");

                listeData.add(newEntry);

                doc.setProperties(SET_SCHEMA, properties);
            }

        } catch (ClientException e) {
            throw new EwServiceException(e);
        }
        return uri;
    }

}
