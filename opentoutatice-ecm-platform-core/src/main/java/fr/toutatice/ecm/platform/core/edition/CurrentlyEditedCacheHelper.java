package fr.toutatice.ecm.platform.core.edition;

import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;

import net.sf.json.JSONObject;

/**
 * Helper to add and remove values in currently edited cache
 *
 * @author dorian
 */
public class CurrentlyEditedCacheHelper {

    /** CURRENTLY_EDITED_CACHE_NAME */
    private static final String CURRENTLY_EDITED_CACHE_NAME = "currently-edited-cache";

    private CurrentlyEditedCacheHelper() {
    }

    /**
     * Store the name of the users currently editing and the current timestamp into the cache
     *
     * @param document
     * @param userNames
     */
    public static void put(DocumentModel document, List<String> userNames) {
        EditionCacheHelper.put(document, userNames, CURRENTLY_EDITED_CACHE_NAME);
    }

    /**
     * Store the name of the user currently editing and the current timestamp into the cache
     *
     * @param document
     * @param userNames
     */
    public static void put(DocumentModel document, String userName) {
        EditionCacheHelper.put(document, userName, CURRENTLY_EDITED_CACHE_NAME);
    }

    /**
     * Retrieve the name of the user currently editing and the timestamp he started from the cache
     *
     * @param document
     * @return
     */
    public static JSONObject get(DocumentModel document) {
        return EditionCacheHelper.get(document, CURRENTLY_EDITED_CACHE_NAME);
    }

    /**
     * Invalidate the cache for the given document
     *
     * @param document
     */
    public static void invalidate(DocumentModel document) {
        EditionCacheHelper.invalidate(document, CURRENTLY_EDITED_CACHE_NAME);
    }
}
