package fr.toutatice.ecm.platform.core.edition;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.cache.CacheAttributesChecker;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.runtime.api.Framework;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Helper to add and remove values in currently edited cache
 *
 * @author dorian
 */
public class EditionCacheHelper {

    public static final String USERNAME_KEY = "username";

    public static final String DATE_KEY = "date";

    /** Cache service. */
    private static CacheService cacheService;

    private EditionCacheHelper() {
    }


    /**
     * Store the name of the users currently editing and the current timestamp into the cache
     *
     * @param document
     * @param userNames
     * @param cacheName
     */
    public static void put(DocumentModel document, List<String> userNames, String cacheName) {
        if (document != null) {
            if (CollectionUtils.isNotEmpty(userNames)) {
                JSONObject cacheEntry = buildCacheEntry(userNames);
                try {
                    CacheAttributesChecker cache = getCacheService().getCache(cacheName);
                    if (cache != null) {
                        cache.put(document.getId(), cacheEntry);
                    }
                } catch (IOException e) {
                    // NOP
                }
            }
        }
    }

    /**
     * Store the name of the user currently editing and the current timestamp into the cache
     *
     * @param document
     * @param userNames
     * @param cacheName
     */
    public static void put(DocumentModel document, String userName, String cacheName) {
        if (document != null) {
            if (StringUtils.isNotBlank(userName)) {
                JSONObject cacheEntry = buildCacheEntry(userName);
                try {
                    CacheAttributesChecker cache = getCacheService().getCache(cacheName);
                    if (cache != null) {
                        cache.put(document.getId(), cacheEntry);
                    }
                } catch (IOException e) {
                    // NOP
                }
            }
        }
    }

    /**
     * Retrieve the name of the user currently editing and the timestamp he started from the cache
     *
     * @param document
     * @param cacheName
     * @return
     */
    public static JSONObject get(DocumentModel document, String cacheName) {
        JSONObject cacheEntry = null;
        if (document != null) {
            try {
                CacheAttributesChecker cache = getCacheService().getCache(cacheName);
                if (cache != null) {
                    cacheEntry = (JSONObject) cache.get(document.getId());
                }
            } catch (IOException e) {
                // NOP
            }
        }
        return cacheEntry;
    }

    /**
     * Invalidate the cache for the given document
     *
     * @param document
     * @param cacheName
     */
    public static void invalidate(DocumentModel document, String cacheName) {
        if (document != null) {
            try {
                CacheAttributesChecker cache = getCacheService().getCache(cacheName);
                if (cache != null) {
                    cache.invalidate(document.getId());
                }
            } catch (IOException e) {
                // NOP
            }
        }
    }

    /**
     * format an entry to be stored in the cache
     *
     * @param userNames
     * @param cacheName
     * @return
     */
    private static JSONObject buildCacheEntry(List<String> userNames) {
        JSONObject cacheEntry = new JSONObject();
        cacheEntry.put(DATE_KEY, new Date().getTime());
        JSONArray usernamesArray = new JSONArray();
        usernamesArray.addAll(userNames);
        cacheEntry.put(USERNAME_KEY, usernamesArray);
        return cacheEntry;
    }

    /**
     * format an entry to be stored in the cache
     *
     * @param userName
     * @param cacheName
     * @return
     */
    private static JSONObject buildCacheEntry(String userName) {
        JSONObject cacheEntry = new JSONObject();
        cacheEntry.put(DATE_KEY, new Date().getTime());
        JSONArray usernamesArray = new JSONArray();
        usernamesArray.add(userName);
        cacheEntry.put(USERNAME_KEY, usernamesArray);
        return cacheEntry;
    }

    /** Getter for Cache service. */
    private static CacheService getCacheService() {
        if (cacheService == null) {
            cacheService = Framework.getService(CacheService.class);
        }
        return cacheService;
    }

}
