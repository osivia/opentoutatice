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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentLocationImpl;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * @author David Chevrier
 * 
 */
public class ToutaticeDocumentLocation extends DocumentLocationImpl {

    private static final long serialVersionUID = -1632965927936780332L;
    private static final Log log = LogFactory.getLog(ToutaticeDocumentLocation.class);


    private String serverName;
    private WebIdRef webIdRef;

    public String getServerName() {
        return serverName;
    }

    public WebIdRef getWebIdRef() {
        return webIdRef;
    }

    public ToutaticeDocumentLocation(final String serverName, final WebIdRef docRef) {
        super(serverName, docRef);
        this.serverName = serverName;
        this.webIdRef = docRef;
    }

    public ToutaticeDocumentLocation(DocumentModel doc) {
        super(doc);
        try {
            String webId = getLogicalWebId(doc);
            String explicitUrl = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXPLICIT_URL);
            String extensionUrl = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXTENSION_URL);
            this.webIdRef = new WebIdRef(explicitUrl, webId, extensionUrl);
        } catch (Exception e) {
            log.error("Can not get webId property: " + e.getMessage());
        }

    }

    public static String getLogicalWebId(DocumentModel doc) throws Exception {
        String wId = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);

        if (wId != null) {
            // Remote proxy case
            if (doc.isProxy() && doc.hasFacet(ToutaticeNuxeoStudioConst.CST_FACET_REMOTE_PROXY)) {
                // Get webId of parent
                DocumentModel parent = ToutaticeDocumentHelper.getUnrestrictedParent(doc);
                if (parent != null) {
                    String pWId = (String) parent.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);

                    if (pWId != null) {
                        // Concatenation rule
                        wId = wId.concat(WebIdResolver.RPXY_WID_MARKER).concat(pWId);
                    } else {
                        throw new Exception("Document " + parent.getPathAsString() + " has no webId.");
                    }
                } else {
                    // Root case only?
                    log.warn("Document " + doc.getPathAsString() + " has no parent");
                }
            }
        }

        return wId;
    }
}
