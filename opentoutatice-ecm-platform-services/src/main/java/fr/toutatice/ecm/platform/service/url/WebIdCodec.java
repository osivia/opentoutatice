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
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.service.url;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.service.AbstractDocumentViewCodec;

import fr.toutatice.ecm.platform.core.constants.ToutaticeUtilsConst;

/**
 * 
 * Codec and Uncodec for webid : url pattern is /web/domain-id/resource-name
 * 
 * @author loic
 * 
 */
public class WebIdCodec extends AbstractDocumentViewCodec {

    private static final Log log = LogFactory.getLog(WebIdCodec.class);

    public static final String CONTENT_PARAM = "content";
    public static final String PREFIX = "web/";
    private static final String DEFAULT_REPO = "default";

    // /web/resource-name
    // public static final String URLPattern = "/" +
    // "([a-zA-Z_0-9\\-\\.]+)" + // weburl
    // "(/)?" + "(.*)?"; // params

    // web/path/or/string/explicit/webid{.extension}{?param=value}

    public static final String DOC_TYPE = "DOC_TYPE";
    public static final String FILE_PROPERTY_PATH_KEY = "FILE_PROPERTY_PATH";
    public static final String FILENAME_KEY = "FILENAME";
    

    public WebIdCodec() {
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

        // Pattern :
        // web/path/or/string/explicit/webid{.extension}{?param=value}
        if (url.startsWith(PREFIX)) {
            url = url.substring(PREFIX.length());

            String[] segments = url.split("/");
            if (segments.length >= 1) {
                String lastSegment = segments[segments.length - 1];
                String webid;
                String extensionUrl = null;
                String explicitUrl = null;

                int extMarker = StringUtils.indexOf(lastSegment, ".");
                int paramMarker = StringUtils.indexOf(lastSegment, "?");

                if (extMarker > -1) {
                    webid = StringUtils.substring(lastSegment, 0, extMarker);
                    if (paramMarker > -1) {
                        extensionUrl = StringUtils.substring(lastSegment, extMarker, paramMarker);
                    } else {
                        extensionUrl = StringUtils.substring(lastSegment, extMarker, lastSegment.length());
                    }
                } else if (paramMarker > -1) {
                    webid = StringUtils.substring(lastSegment, 0, paramMarker);
                } else {
                    webid = lastSegment;
                }

                if (segments.length >= 3) {
                    List<String> items = new ArrayList<String>();
                    for (int index = 1; index < segments.length - 2; index++) {
                        items.add(segments[index]);
                    }
                    explicitUrl = StringUtils.join(items, "/");
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
                
                Map<String, String> parameters = new HashMap<String, String>();
                if (params != null) {
                    String content = params.get(CONTENT_PARAM);
                    if (content != null) {
                        /*
                         * content is always defined for Picture in case of tiny-mce
                         * source link
                         */
                        parameters.put(FILE_PROPERTY_PATH_KEY, content.concat(":content"));
                    } else {
                        parameters.put(FILE_PROPERTY_PATH_KEY, "Original:content");
                    }
                    parameters.put(FILENAME_KEY, webid);
                    parameters.putAll(params);
                }

                /* FIXME: replace DEFAULT_REPO by server name? */
                final ToutaticeDocumentLocation docLoc = new ToutaticeDocumentLocation(DEFAULT_REPO, new WebIdRef(explicitUrl, webid, extensionUrl, parameters));
                /* FIXME: find view instead hard coding view_documents */
                DocumentViewImpl documentViewImpl = new DocumentViewImpl(docLoc, "view_documents", parameters);
                return documentViewImpl;

            }
        }

        return null;
    }

    @Override
    public String getUrlFromDocumentView(DocumentView docView) {

        ToutaticeDocumentLocation docLoc = (ToutaticeDocumentLocation) docView.getDocumentLocation();
        WebIdRef webIdRef = docLoc.getWebIdRef();
        //String extensionUrl = webIdRef.getExtensionUrl();
        String webId = (String) webIdRef.reference();
        String explicitUrl = webIdRef.getExplicitUrl();

        if (StringUtils.isNotBlank(webId)) {
            List<String> items = new ArrayList<String>();
            items.add(getPrefix());
            if (StringUtils.isNotBlank(explicitUrl)) {
                items.add(explicitUrl);
            }
            items.add(webId);

			String uri = StringUtils.join(items, ToutaticeUtilsConst.PATH_SEPARATOR);
			
            Map<String, String> viewParameters = docView.getParameters();
            return URIUtils.addParametersToURIQuery(uri, viewParameters);
        }

        return null;
    }

}
