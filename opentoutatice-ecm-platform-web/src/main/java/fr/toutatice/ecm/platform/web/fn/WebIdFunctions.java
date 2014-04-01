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
 * lbillon
 */
package fr.toutatice.ecm.platform.web.fn;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentLocation;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.web.urlservice.WebIdCodec;

/**
 * Functions for making webid's urls in views
 * 
 * @author lbillon
 * 
 */
public class WebIdFunctions {

    private static final Log log = LogFactory.getLog(WebIdCodec.class);

    private static final String BASE_URL = "";
    private static final String PATTERN = "webidpattern";

    /**
     * Return true if document has a webid defined
     * 
     * @param doc the current doc
     * @return webid defined
     */
    public static boolean hasWebId(DocumentModel doc) {

        boolean ret = false;
        try {
            if (doc != null) {
                Object webid = doc.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue();
                if (webid != null && StringUtils.isNotBlank(webid.toString())) {

                    ret = true;
                }
            }
        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        }
        return ret;
    }


    /**
     * Return the url by webid or the path (if not defined)
     * 
     * @param doc
     * @return
     */
    public static String getPreferredLinkUrl(DocumentModel doc) {

        String url = "";

        try {
            Object webid = doc.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue();
            if (webid != null && StringUtils.isNotBlank(webid.toString())) {

                url = callCodec(doc, null);
            } else {
                return doc.getPathAsString();
            }

        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        } 
        return url;
    }
    
    /**
     * Return the url by webid or the path (if not defined)
     * 
     * @param patternName
     * @param doc
     * @param blobPropertyName
     * @param filename
     * @return
     */
    public static String getPreferredImgUrl(String patternName, DocumentModel doc, String blobPropertyName, String filename) {

        String url = "";

        try {
            Object webid = doc.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue();
            if (webid != null && StringUtils.isNotBlank(webid.toString())) {

                url = callCodec(doc, blobPropertyName);
            } else {
                return DocumentModelFunctions.fileUrl(patternName, doc, blobPropertyName, filename);

            }

        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        }
        return url;
    }

    /**
     * Translate webid in url
     * 
     * @param doc
     * @param blobPropertyName
     * @return
     */
    private static String callCodec(DocumentModel doc, String blobPropertyName) {

        String url = "";

        try {
            URLPolicyService service = Framework.getService(URLPolicyService.class);

            DocumentLocation docLoc = new DocumentLocationImpl(doc);
            Map<String, String> params = new HashMap<String, String>();

            String webid = doc.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).getValue().toString();

            params.put(WebIdCodec.WEBID_KEY, webid.toString());

            if (blobPropertyName != null) {
                params.put(WebIdCodec.FILE_PROPERTY_PATH_KEY, blobPropertyName);
            }

            DocumentView docView = new DocumentViewImpl(docLoc, null, params);

            url = service.getUrlFromDocumentView(PATTERN, docView, BASE_URL);
        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        } catch (Exception e) {
            log.error("Erreur génération webid " + e);
        }

        return url;
    }
}
