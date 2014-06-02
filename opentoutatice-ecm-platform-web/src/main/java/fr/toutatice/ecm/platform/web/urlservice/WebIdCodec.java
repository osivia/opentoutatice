/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * 
 * Contributors:
 * mberhaut1
 */
package fr.toutatice.ecm.platform.web.urlservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.service.AbstractDocumentViewCodec;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 * Codec and Uncodec for webid : url pattern is /web/domain-id/resource-name
 * 
 * @author loic
 * 
 */
public class WebIdCodec extends AbstractDocumentViewCodec {

    public static final String WEBID_KEY = "WEBID";
    public static final String DOMAINID_KEY = "DOMAINID";
    public static final String EXPLICIT_KEY = "EXPLICIT";
    public static final String EXT_KEY = "EXT";

    public static final String CONTENT_PARAM = "content";

    public static final String PREFIX = "web/";

    private static final Log log = LogFactory.getLog(WebIdCodec.class);

    private CoreSession documentManager;

    private static final String DEFAULT_REPO = "default";




    // /web/domain-id/resource-name
    // public static final String URLPattern = "/" +
    // "([a-zA-Z_0-9\\-]+)/" + // domain
    // "([a-zA-Z_0-9\\-\\.]+)" + // weburl
    // "(/)?" + "(.*)?"; // params

    // web/domain-id/path/or/string/explicit/webid{.extension}{?param=value}


    public static final String DOC_TYPE = "DOC_TYPE";
    public static final String FILE_PROPERTY_PATH_KEY = "FILE_PROPERTY_PATH";
    public static final String FILENAME_KEY = "FILENAME";




    public WebIdCodec() {
        try {
            // get a documentManager for the queries
            Framework.login();
            RepositoryManager mgr = Framework.getService(RepositoryManager.class);
            Repository repository = mgr.getDefaultRepository();
            if (repository != null) {
                documentManager = repository.open();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPrefix() {
        if (prefix != null) {
            return prefix;
        }
        return PREFIX;
    }


    @Override
    public DocumentView getDocumentViewFromUrl(String url) {


        // Pattern : web/domain-id/path/or/string/explicit/webid{.extension}{?param=value}
        if (url.startsWith(PREFIX)) {
            url = url.substring(PREFIX.length());

            String[] segments = url.split("/");
            if (segments.length >= 2) {
                String domainID = segments[0];
                String lastSegment = segments[segments.length - 1];
                String webid;

                int extMarker = StringUtils.indexOf(lastSegment, ".");
                int paramMarker = StringUtils.indexOf(lastSegment, "?");

                if (extMarker > -1) {
                    webid = StringUtils.substring(lastSegment, 0, extMarker);
                } else if (paramMarker > -1) {
                    webid = StringUtils.substring(lastSegment, 0, paramMarker);
                } else {
                    webid = lastSegment;
                }

                Map<String, String> params = null;
                if (paramMarker > -1) {

                    params = new HashMap<String, String>();
                    String paramsStr = StringUtils.substringAfter(lastSegment, "?");
                    String[] paramsArr = StringUtils.split(paramsStr, "&");

                    for (int i = 0; i < paramsArr.length; i++) {
                        String[] pair = paramsArr[i].split("=");

                        if (pair.length == 2) {
                            params.put(pair[0], pair[1]);
                        }
                    }
                }

                DocumentModelList docs = null;
                try {

                    docs = documentManager.query("SELECT * FROM Document where ttc:webid = '" + webid + "' AND ttc:domainID = '" + domainID + "'"
                            + " AND ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0");

                } catch (ClientException e) {
                    log.error("Impossible de déterminer le webID " + e);
                }


                if (docs.size() == 1 && docs.get(0) != null) {

                    DocumentModel doc = docs.get(0);
                    DocumentRef docRef = doc.getRef(); // get the doc who matches the webid

                    final DocumentLocation docLoc = new DocumentLocationImpl(DEFAULT_REPO, docRef);

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put(DOC_TYPE, doc.getType());

                    String view = null;
                    if (doc.getType().equals("Picture")) {

                        if (params != null) {
                            String content = params.get("content");
                            if (content != null) {
                                parameters.put(FILE_PROPERTY_PATH_KEY, content.concat(":content"));
                            } else {
                                parameters.put(FILE_PROPERTY_PATH_KEY, "Original:content");
                            }
                        } else {
                            parameters.put(FILE_PROPERTY_PATH_KEY, "Original:content");
                        }

                        parameters.put(FILENAME_KEY, webid);
                    } else if (doc.getType().equals("File")) {
                        parameters.put(FILE_PROPERTY_PATH_KEY, "file:content");
                        parameters.put(FILENAME_KEY, webid);
                    } else {
                        view = "view_documents";
                    }

                    if (params != null) {
                        parameters.putAll(params);
                    }

                    DocumentViewImpl documentViewImpl = new DocumentViewImpl(docLoc, view, parameters);
                    return documentViewImpl;

                } else {
                    log.error("More than one document with webid: " + webid);
                }


            }
        }

        return null;
    }

    @Override
    public String getUrlFromDocumentView(DocumentView docView) {

        String webid = docView.getParameter(WEBID_KEY);
        String domainID = docView.getParameter(DOMAINID_KEY);
        String explicit = docView.getParameter(EXPLICIT_KEY);
        String ext = docView.getParameter(EXT_KEY);

        if (domainID != null && webid != null) {
            List<String> items = new ArrayList<String>();

            Map<String, String> requestParams = new HashMap<String, String>(docView.getParameters());
            requestParams.remove(WEBID_KEY);
            requestParams.remove(DOMAINID_KEY);

            items.add(getPrefix()); // /web
            items.add(domainID); // /domainID

            if (explicit != null) {
                items.add(explicit); // /path/explicit
                requestParams.remove(EXPLICIT_KEY);
            }

            items.add(webid); // /webid

            String uri = StringUtils.join(items, "/");

            if (ext != null) {
                uri = uri.concat(".").concat(ext); // extension
                requestParams.remove(EXT_KEY);
            }


            docView.getParameters();

            String ret = URIUtils.addParametersToURIQuery(uri, requestParams);
            return ret;
        }


        return null;
    }

}
