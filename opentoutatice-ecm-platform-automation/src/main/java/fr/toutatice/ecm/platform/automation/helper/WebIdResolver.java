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
package fr.toutatice.ecm.platform.automation.helper;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;


/**
 * Manage the resolution of a document given its webId.
 * 
 * @author david chevrier
 *
 */
public class WebIdResolver {
    
    /** Itility class. */
    private WebIdResolver(){};
    
    /** Query to get document according to its webId. */
    private static final String WEB_ID_QUERY = "select * from Document Where ttc:domainID = '%s'"
            + " AND ttc:webid = '%s' %s AND ecm:currentLifeCycleState!='deleted' AND ecm:isCheckedInVersion = 0";
    
    public static DocumentModel getDocumentByWebId(CoreSession coreSession, String webid) throws NoSuchDocumentException {
        
        DocumentModel document = null;
        
        if (webid != null) {
            
            String[] segments = webid.split("/");
            String domainIdSegment;
            String webIdSegment;
            if (segments.length >= 2) {
                domainIdSegment = segments[0];
                webIdSegment = segments[1];
            } else {
                throw new NoSuchDocumentException(webid);
            }
            
            
            UnrestrictedFecthWebIdRunner fecthWebIdRunner = new UnrestrictedFecthWebIdRunner(coreSession, domainIdSegment, webIdSegment);
            fecthWebIdRunner.runUnrestricted();
            document = fecthWebIdRunner.getDoc();
            if (document == null) {
                throw new NoSuchDocumentException(webid);
            }
            
        }
        
        return document;

    }
    
    /**
     * Get doc by webid in unrestricted mode (admin)
     */
    private static class UnrestrictedFecthWebIdRunner extends UnrestrictedSessionRunner {
        
        String webIdSegment;
        String domainIdSegment;
        DocumentModel docResolved;

        private static final String PROXY_FILTER = " AND ecm:isProxy = 1 ";
        private static final String LIVE_FILTER = " AND ecm:isProxy = 0 ";

        public UnrestrictedFecthWebIdRunner(CoreSession session, String domainId, String webId) {
            super(session);
            this.webIdSegment = webId;
            this.domainIdSegment = domainId;

        }

        @Override
        public void run() throws ClientException {
            DocumentModelList liveDocs = this.session.query(String.format(WEB_ID_QUERY, this.domainIdSegment, this.webIdSegment, LIVE_FILTER));
            if (liveDocs.size() == 1) {
                this.docResolved = liveDocs.get(0);
            } else if (liveDocs.size() == 0) {
                DocumentModelList proxiesDocs = this.session.query(String.format(WEB_ID_QUERY, this.domainIdSegment, this.webIdSegment, PROXY_FILTER));
                if (proxiesDocs.size() == 1) {
                    this.docResolved = proxiesDocs.get(0);
                } else if (proxiesDocs.size() > 1) {
                    throw new ClientException("Two or more published documents have the webid : " + this.webIdSegment);
                }
            } else if (liveDocs.size() > 1) {
                throw new ClientException("Two or more live documents have the webid : " + this.webIdSegment);
            }
        }

        public DocumentModel getDoc() {
            return this.docResolved;
        }

    }


}
