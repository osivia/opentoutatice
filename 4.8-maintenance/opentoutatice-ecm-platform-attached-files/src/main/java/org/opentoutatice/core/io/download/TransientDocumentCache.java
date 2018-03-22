package org.opentoutatice.core.io.download;

import java.util.concurrent.ConcurrentHashMap;

import org.nuxeo.ecm.core.api.DocumentModel;
/**
 * 
 * @author david
 */
public class TransientDocumentCache {

    private static ConcurrentHashMap<String, DocumentModel> cache;

    private TransientDocumentCache() {
    };

    private static ConcurrentHashMap<String, DocumentModel> get() {
        if (cache == null) {
            cache = new ConcurrentHashMap<String, DocumentModel>();
        }
        return cache;
    }

    public static void put(String key, DocumentModel doc) {
        get().put(key, doc);
    }
    
    public static DocumentModel getChangeablDocument(String key) {
        return get().get(key);
    }

    public static void invalidate(String key) {
        get().remove(key);
    }

}