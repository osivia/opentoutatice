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
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ui.web.rest.api.URLPolicyService;
import org.nuxeo.ecm.platform.ui.web.tag.fn.DocumentModelFunctions;
import org.nuxeo.ecm.platform.ui.web.util.BaseURL;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentView;
import org.nuxeo.ecm.platform.url.codec.DocumentFileCodec;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.service.url.ToutaticeDocumentLocation;
import fr.toutatice.ecm.platform.service.url.WebIdCodec;
import fr.toutatice.ecm.platform.service.url.WebIdResolver;

/**
 * Functions for making webid's urls in views
 * 
 * @author lbillon
 * 
 */
public class WebIdFunctions {

    /** Path URL pattern indicator. */
    private static final String PATH_PATTERN = "default";
    /** WebId url pattern. */
    private static final String WEBID_PATTERN = "webidpattern";

    private static final Log log = LogFactory.getLog(WebIdFunctions.class);

    private static final String WEBID_DOWNLOAD_PICTURE = "webiddownloadpicture";

    /** Portal share link marker. */
    private static final String PSL_MARKER = "?l=";

    /** URL service. */
    protected static URLPolicyService urlService;

    /**
     * Getter for URLPolicyService.
     */
    public static URLPolicyService getURLPolicyService() {
        if (urlService == null) {
            urlService = Framework.getService(URLPolicyService.class);
        }
        return urlService;
    }

    /**
     * Return true if document has a webid defined
     * 
     * @param doc
     *            the current doc
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

        String url = StringUtils.EMPTY;

        try {
            String webid = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
            if (StringUtils.isNotBlank(webid)) {
                url = callWebIdCodec(doc, null);
            } else {
                url = DocumentModelFunctions.documentUrl(doc);
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

        String url = StringUtils.EMPTY;

        try {
            String webid = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
            if (StringUtils.isNotBlank(webid)) {
                url = callWebIdCodec(doc, blobPropertyName);
            } else {
                url = DocumentModelFunctions.fileUrl(patternName, doc, blobPropertyName, filename);
            }

        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        }
        return url;
    }

    /**
     * @param document
     * @return the webid or path of a document.
     */
    public static String getPreferredDisplayId(DocumentModel doc) {
        String id = StringUtils.EMPTY;

        try {
            String webid = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
            if (StringUtils.isNotBlank(webid)) {
                id = StringUtils.replace(ToutaticeDocumentLocation.getLogicalWebId(doc), WebIdResolver.RPXY_WID_MARKER, PSL_MARKER);
            } else {
                id = doc.getPathAsString();
            }

        } catch (Exception e) {
            log.error("Erreur génération webid " + e);
        }
        return id;
    }

    /**
     * Use of URLPolicy service to find absolute URL from document.
     * 
     * @param doc
     * @param blobPropertyName
     * @return absolute URL
     */
    private static String callWebIdCodec(DocumentModel doc, String blobPropertyName) {

        String url = StringUtils.EMPTY;

        try {
            Map<String, String> parameters = new HashMap<String, String>();
            if ("Picture".equals(doc.getType())) {
                if (StringUtils.isNotBlank(blobPropertyName)) {
                    parameters.put(WebIdCodec.CONTENT_PARAM, StringUtils.replace(blobPropertyName, ":content", StringUtils.EMPTY));
                }
            }

            ToutaticeDocumentLocation webIdDocLoc = new ToutaticeDocumentLocation(doc);
            DocumentView docView = new DocumentViewImpl(webIdDocLoc, null, parameters);
            url = getURLPolicyService().getUrlFromDocumentView(WEBID_PATTERN, docView, BaseURL.getBaseURL());

        } catch (ClientException e) {
            log.error("Erreur génération webid " + e);
        } catch (Exception e) {
            log.error("Erreur génération webid " + e);
        }

        return url;

    }


    // /**
    // * Translate webid in url
    // *
    // * @param doc
    // * @param blobPropertyName
    // * @return
    // */
    // private static String callCodec(DocumentModel doc, String blobPropertyName) {
    //
    // String url = StringUtils.EMPTY;
    //
    // try {
    // URLPolicyService service = Framework.getService(URLPolicyService.class);
    //
    // String pattern = WEBID_PATTERN;
    // Map<String, String> params = new HashMap<String, String>();
    // if ("Picture".equals(doc.getType())) {
    // pattern = WEBID_DOWNLOAD_PICTURE;
    // params.put(WebIdCodec.CONTENT_PARAM, StringUtils.replace(blobPropertyName, ":content", ""));
    // }
    //
    // ToutaticeDocumentLocation webIdDocLoc = new ToutaticeDocumentLocation(doc);
    // DocumentView docView = new DocumentViewImpl(webIdDocLoc, null, params);
    // url = service.getUrlFromDocumentView(pattern, docView, BASE_URL);
    //
    // } catch (ClientException e) {
    // log.error("Erreur génération webid " + e);
    // } catch (Exception e) {
    // log.error("Erreur génération webid " + e);
    // }
    //
    // return url;
    // }

    protected static DocumentView getDownloadFileProperties(DocumentLocation docLoc, DocumentModel doc) throws PropertyException, ClientException {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(DocumentFileCodec.FILE_PROPERTY_PATH_KEY, "file:content");
        String fileName = (String) doc.getPropertyValue("file:filename");

        parameters.put(DocumentFileCodec.FILENAME_KEY, fileName);
        return new DocumentViewImpl(docLoc, null, parameters);
    }

    protected static DocumentView getDownloadPictureProperties(DocumentLocation docLoc, DocumentModel doc, String blobPropertyName) throws PropertyException,
            ClientException {
        Map<String, String> parameters = new HashMap<String, String>();
        if (StringUtils.isNotBlank(blobPropertyName)) {
            parameters.put(DocumentFileCodec.FILE_PROPERTY_PATH_KEY, blobPropertyName);
        } else {
            parameters.put(DocumentFileCodec.FILE_PROPERTY_PATH_KEY, "Original:content");
        }
        String fileName = doc.getPropertyValue("dc:modified").toString();
        parameters.put(DocumentFileCodec.FILENAME_KEY, fileName);
        return new DocumentViewImpl(docLoc, null, parameters);
    }
}
