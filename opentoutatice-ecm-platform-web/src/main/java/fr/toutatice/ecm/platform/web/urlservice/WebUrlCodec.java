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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.core.api.repository.Repository;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.service.AbstractDocumentViewCodec;
import org.nuxeo.runtime.api.Framework;


public class WebUrlCodec extends AbstractDocumentViewCodec {

    private static final Log log = LogFactory.getLog(WebUrlCodec.class);

    private CoreSession documentManager;

    private static final String DEFAULT_REPO = "default";

    public static final String PREFIX = "nxurl";

    // nxurl/server/domain/weburl@view_id?requestParams
    public static final String URL_PATTERN = "/" // slash
            + "([\\w\\.]+)" // server name (group 1)
            + "/" + "([\\w\\.]+)" // domain name (group 2)
            + "/" + "([\\w\\.]+)" // weburl name (group 3)
            + "@([\\w\\-\\.]+)" // view id (group 4)
            + "/?" // final slash (optional)
            + "(?:\\?(.*)?)?"; // query (group 5) (optional)

    // nxurl/server/docId/property_path/filename/?requestParams
    // nxurl/default/default-domain/note
    public static final String URLPattern = "/" + "(\\w+)/" + // server
            "([a-zA-Z_0-9\\-]+)/" + // domain
            "([a-zA-Z_0-9\\-]+)" + // weburl
            // "(/([a-zA-Z_0-9/:\\-\\.\\]\\[]*))+" +
            // "(/([^\\?]*))" +
            "+(\\?)?(.*)?";


    public static final String simpleURLPattern = "/" + "([a-zA-Z_0-9\\-]+)" + // weburl
            "+(\\?)?(.*)?"; // options


    public WebUrlCodec() {
        try {
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
        final Pattern pattern = Pattern.compile(getPrefix() + simpleURLPattern);
        Matcher m = pattern.matcher(url);
        if (m.matches()) {

            String webid = m.group(1);

            log.warn("webid : " + webid);

            DocumentModelList docs = null;
            try {

                docs = documentManager.query("SELECT * FROM Document where ttc:webid = '" + webid + "'");

            } catch (ClientException e) {
                log.error("Impossible de déterminer la weburl " + e);
            }


            if (docs.size() == 1 && docs.get(0) != null) {

                DocumentRef docRef = docs.get(0).getRef();
                String viewId = m.group(3);
                if (viewId.length() > 1) {
                    viewId = viewId.substring(1);
                }
                Map<String, String> params = new HashMap<String, String>();
                // get other parameters
                if (m.groupCount() > 3) {
                    String parameters = m.group(4);
                    params = URIUtils.getRequestParameters(parameters);
                }
                final DocumentLocation docLoc = new DocumentLocationImpl(DEFAULT_REPO, docRef);

                DocumentViewImpl documentViewImpl = new DocumentViewImpl(docLoc, viewId, params);
                log.warn("fin méthode webid");
                return documentViewImpl;

            } else {
                log.error("More than one document with webid: " + webid);
            }
        }

        return null;
    }

    @Override
    public String getUrlFromDocumentView(DocumentView docView) {
        log.error("getUrlFromDocumentView non implémenté ");

        return null;
    }

}
