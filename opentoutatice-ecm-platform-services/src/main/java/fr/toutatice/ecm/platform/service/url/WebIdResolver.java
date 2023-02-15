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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.service.url;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;

import fr.toutatice.ecm.platform.core.helper.ToutaticeQueryHelper;


/**
 * Manage the resolution of a document given its webId.
 * 
 * @author david chevrier
 *
 */
public class WebIdResolver {
    
    /** Logger. */
    private static final Log log = LogFactory.getLog(WebIdResolver.class);

    /** Utility class. */
    private WebIdResolver() {
    };

    /**
     * @param coreSession
     * @param webId
     * @return unique live document given its webid.
     */
    public static DocumentModel getLiveDocumentByWebId(CoreSession coreSession, String webId) {
        DocumentModel live = null;

        String query = String.format(ToutaticeWebIdHelper.LIVE_WEB_ID_QUERY, webId);
        DocumentModelList lives = ToutaticeQueryHelper.queryUnrestricted(coreSession, query);
        if (CollectionUtils.isNotEmpty(lives) && lives.size() == 1) {
            live = lives.get(0);
        }

        return live;
    }

    /**
     * @param coreSession
     * @param webId
     * @return documents matching given webid (live or proxy, proxies).
     * @throws NoSuchDocumentException
     */
    public static DocumentModelList getDocumentsByWebId(CoreSession coreSession, String webId)
            throws NoSuchDocumentException {

        DocumentModelList documents = null;

        if (StringUtils.isNotBlank(webId)) {
            // For Trace logs
            long begin = System.currentTimeMillis();

            UnrestrictedFecthWebIdRunner fecthWebIdRunner = new UnrestrictedFecthWebIdRunner(coreSession, webId);
            fecthWebIdRunner.runUnrestricted();
            documents = fecthWebIdRunner.getDocuments();
            
            if(log.isTraceEnabled()){
                long end = System.currentTimeMillis();
                log.trace(": " + String.valueOf(end - begin) + " ms");
            }

            if (CollectionUtils.isEmpty(documents)) {
                throw new NoSuchDocumentException(webId);
            }
            else if (documents.size() > 1) {

                String paths = "";
                for(DocumentModel document : documents) {
                    paths = paths.concat(document.getPathAsString()).concat(", ");
                }

                log.warn("Multiple documents found for webid "+webId+" "+paths);
            }

        }

        return documents;

    }

    /**
     * Get doc by webid in unrestricted mode (admin)
     */
    private static class UnrestrictedFecthWebIdRunner extends UnrestrictedSessionRunner {
        
        String webId;
        DocumentModelList documents;

        public UnrestrictedFecthWebIdRunner(CoreSession session, String webId) {
            super(session);
            this.webId = webId;
            this.documents = new DocumentModelListImpl();
        }

        @Override
        public void run() throws ClientException {
            getLive();
        }

        /**
         * Get live with given webId.
         */
        private void getLive() {
            DocumentModelList lives = this.session.query(String.format(ToutaticeWebIdHelper.LIVE_WEB_ID_QUERY, this.webId));
            if (CollectionUtils.isNotEmpty(lives)) {
                this.documents = lives;
            }
        }


        public DocumentModelList getDocuments() {
            return this.documents;
        }

    }


}
