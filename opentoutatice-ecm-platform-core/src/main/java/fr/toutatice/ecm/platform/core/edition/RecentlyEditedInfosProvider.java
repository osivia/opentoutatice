package fr.toutatice.ecm.platform.core.edition;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;
import net.sf.json.JSONObject;

/**
 * provides informations if the document is being edited in onlyoffice or nuxeo drive
 *
 * @author dorian
 */
public class RecentlyEditedInfosProvider implements DocumentInformationsProvider {

    public static final String IS_RECENTLY_EDITED_KEY = "isRecentlyEdited";

    public static final String RECENTLY_EDITED_KEY = "recentlyEditedEntry";

    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

        Map<String, Object> infos = null;

        JSONObject cacheEntry = RecentlyEditedCacheHelper.get(currentDocument);
        if (cacheEntry != null) {
            infos = new HashMap<>(2);
            infos.put(IS_RECENTLY_EDITED_KEY, true);
            infos.put(RECENTLY_EDITED_KEY, cacheEntry);
        } else {
            infos = new HashMap<>(1);
            infos.put(IS_RECENTLY_EDITED_KEY, false);
        }

        return infos;
    }

}
