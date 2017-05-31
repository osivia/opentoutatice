/**
 * 
 */
package org.opentoutatice.ecm.attached.files.url.codec;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.seam.core.Manager;
import org.nuxeo.common.utils.StringUtils;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;


/**
 * @author david
 *
 */
public class OttcAttachedFileCodec extends DocumentFileCodec {
    
    public final static String CREATING_DOC_INDICATOR = "creatingDoc";
    
    // nxfile/server/creationMode/property_path/filename/?requestParams
    public static final String URLPattern = "/(\\w+)/([a-zA-Z_0-9\\-]*|creatingDoc)(/([a-zA-Z_0-9/:\\-\\.\\]\\[]*))+(/([^\\?]*))+(\\?)?(.*)?";

    /**
     * 
     */
    public OttcAttachedFileCodec() {
        super();
    }

    /**
     * @param prefix
     */
    public OttcAttachedFileCodec(String prefix) {
        super(prefix);
    }
    
    @Override
    public String getUrlFromDocumentView(DocumentView docView) {
        DocumentLocation docLoc = docView.getDocumentLocation();
        String filepath = docView.getParameter(FILE_PROPERTY_PATH_KEY);
        String filename = docView.getParameter(FILENAME_KEY);
        if (docLoc != null && filepath != null && filename != null) {
            List<String> items = new ArrayList<String>();
            items.add(getPrefix());
            if(org.apache.commons.lang.StringUtils.isBlank(docLoc.getServerName())){
                items.add("default");
            } else {
                items.add(docLoc.getServerName());
            }
            if(docLoc.getDocRef() == null){
                items.add(CREATING_DOC_INDICATOR);
            } else{
                items.add(docLoc.getDocRef().toString());
            }
            items.add(filepath);
            items.add(URIUtils.quoteURIPathToken(filename));
            String uri = StringUtils.join(items, "/");

            Map<String, String> requestParams = new HashMap<String, String>(
                    docView.getParameters());
            requestParams.remove(FILE_PROPERTY_PATH_KEY);
            requestParams.remove(FILENAME_KEY);
            // To keep documentActions Seam conversation (#upload)
            if(docLoc.getDocRef() == null){
                Manager conversationManager = (Manager) SeamComponentCallHelper.getSeamComponentByName("org.jboss.seam.core.manager");
                requestParams.put(conversationManager.getConversationIdParameter(), conversationManager.getCurrentConversationId());
            }
            return URIUtils.addParametersToURIQuery(uri, requestParams);
        }
        return null;
    }
    
    /**
     * Extracts document location from a Zope-like URL ie :
     * server/path_or_docId/view_id/tab_id .
     */
    @Override
    public DocumentView getDocumentViewFromUrl(String url) {
        final Pattern pattern = Pattern.compile(getPrefix() + URLPattern);
        Matcher m = pattern.matcher(url);
        if (m.matches()) {
            if (m.groupCount() >= 4) {

                // for debug
                // for (int i = 1; i < m.groupCount() + 1; i++) {
                // System.err.println(i + ": " + m.group(i));
                // }

                final String server = m.group(1);
                String uuid = m.group(2);
                
                DocumentRef docRef = null;
                if(!CREATING_DOC_INDICATOR.equals(uuid)){
                    docRef = new IdRef(uuid);
                }

                // get other parameters

                Map<String, String> params = new HashMap<String, String>();
                if (m.groupCount() >= 4) {
                    String filePropertyPath = m.group(4);
                    params.put(FILE_PROPERTY_PATH_KEY, filePropertyPath);
                }

                if (m.groupCount() >= 6) {
                    String filename = m.group(6);
                    try {
                        filename = URLDecoder.decode(filename, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        filename = StringUtils.toAscii(filename);
                    }
                    int jsessionidIndex = filename.indexOf(";jsessionid");
                    if (jsessionidIndex != -1) {
                        filename = filename.substring(0, jsessionidIndex);
                    }
                    params.put(FILENAME_KEY, filename);
                }

                if (m.groupCount() >= 8) {
                    String query = m.group(8);
                    Map<String, String> requestParams = URIUtils.getRequestParameters(query);
                    if (requestParams != null) {
                        params.putAll(requestParams);
                    }
                }

                final DocumentLocation docLoc = new DocumentLocationImpl(
                        server, docRef);

                return new DocumentViewImpl(docLoc, null, params);
            }
        }

        return null;
    }

}
