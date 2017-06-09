/**
 * 
 */
package fr.toutatice.ecm.platform.core.document;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;


/**
 * @author david
 *
 */
public class ParentDocInfosProvider implements DocumentInformationsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        // Infos
        Map<String, Object> infos = new HashMap<String, Object>(1);

        // Parent webId
        String pWebId = null;
        // Get parent
        DocumentModel parent = ToutaticeDocumentHelper.getUnrestrictedParent(currentDocument);
        if (parent != null) {
            pWebId = (String) parent.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
        }

        infos.put("parentWebId", pWebId);
        return infos;
    }

}
