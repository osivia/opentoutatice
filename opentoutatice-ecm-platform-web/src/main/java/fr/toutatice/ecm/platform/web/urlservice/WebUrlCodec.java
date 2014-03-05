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
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.web.urlservice;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nuxeo.common.utils.URIUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.service.AbstractDocumentViewCodec;


public class WebUrlCodec extends AbstractDocumentViewCodec {

    public static final String PREFIX = "nxurl";

    // nxurl/server/domain/weburl@view_id?requestParams
    public static final String URL_PATTERN = "/" // slash
            + "([\\w\\.]+)" // server name (group 1)
            + "/"
            + "([\\w\\.]+)" // domain name (group 2)
            + "/"
            + "([\\w\\.]+)" // weburl name (group 3)
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


    public WebUrlCodec() {


        // try {
        // LoginContext loginContext = Framework.login();
        // RepositoryManager mgr = Framework.getService(RepositoryManager.class);
        // Repository repository = mgr.getDefaultRepository();
        // if (repository != null) {
        // documentManager = repository.open();
        // }
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
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
        final Pattern pattern = Pattern.compile(getPrefix() + URLPattern);
        Matcher m = pattern.matcher(url);
        if (m.matches()) {

            final String server = m.group(1);
            String domain = m.group(2);
            String weburl = m.group(3);
            if (domain != null) {
                // add leading slash to make it absolute if it's not the root
                domain = "/" + URIUtils.unquoteURIPathComponent(domain);
            } else {
                domain = "/";
            }

            DocumentModelList docs = null;
            try {
                WebUrlSearch webUrlSearch = new WebUrlSearch(server, "SELECT * FROM Document where ecm:path startswith '" + domain + "'"
                        + " and ttc:weburl = '" + weburl + "'");
                webUrlSearch.runUnrestricted();

                docs = webUrlSearch.getDocumentPath();
            } catch (ClientException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            if (docs.size() >= 1) {
                final DocumentRef docRef = new PathRef(docs.get(0).getPathAsString());
                final String viewId = m.group(3);

                // get other parameters
                String query = m.group(4);
                Map<String, String> params = URIUtils.getRequestParameters(query);

                final DocumentLocation docLoc = new DocumentLocationImpl(server, docRef);

                return new DocumentViewImpl(docLoc, null, null);
            }
        }

        return null;
    }

    @Override
    public String getUrlFromDocumentView(DocumentView docView) {
        // TODO Auto-generated method stub
        return null;
    }
    
    class WebUrlSearch extends UnrestrictedSessionRunner {

        DocumentModelList query2;
        String weburl;

        public WebUrlSearch(String repositoryName, String weburl) {
            super(repositoryName);

            this.weburl = weburl;
        }
        

        @Override
        public void run() throws ClientException {
            
            query2 = session.query(weburl);
        }
        
        public DocumentModelList getDocumentPath() throws ClientException {
            
            return query2;
        }
    }
}
