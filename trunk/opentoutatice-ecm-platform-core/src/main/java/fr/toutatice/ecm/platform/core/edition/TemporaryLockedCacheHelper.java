package fr.toutatice.ecm.platform.core.edition;

import net.sf.json.JSONObject;

import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Helper to add and remove values in temporary edited cache
 *
 * @author Loïc Billon
 */
public class TemporaryLockedCacheHelper {

    /** CURRENTLY_EDITED_CACHE_NAME */
    public static final String TEMPORARY_LOCKED_CACHE_NAME = "temporary-locked-cache";

    private TemporaryLockedCacheHelper() {
    }


    /**
     * Store the name of the user currently locking and the current timestamp into the cache
     *
     * @param document
     * @param userNames
     */
    public static void put(DocumentModel document, String userName) {
        EditionCacheHelper.put(document, userName, TEMPORARY_LOCKED_CACHE_NAME);
    }

    /**
     * Retrieve the name of the user currently locking and the timestamp he started from the cache
     *
     * @param document
     * @return
     */
    public static JSONObject get(DocumentModel document) {
        return EditionCacheHelper.get(document, TEMPORARY_LOCKED_CACHE_NAME);
    }

    /**
     * Invalidate the cache for the given document
     *
     * @param document
     */
    public static void invalidate(DocumentModel document) {
        EditionCacheHelper.invalidate(document, TEMPORARY_LOCKED_CACHE_NAME);
    }
}
