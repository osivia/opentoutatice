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

import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeQueryHelper;


/**
 * Manage the resolution of a document given its webId.
 * 
 * @author david chevrier
 *
 */
public class WebIdResolver {

    /** Utility class. */
    private WebIdResolver() {
    };

    /** Query to get document according to its webId. */
    private static final String WEB_ID_QUERY = "select * from Document where ttc:webid = '%s'"
            + " %s AND ecm:currentLifeCycleState!='deleted' AND ecm:isCheckedInVersion = 0";

    /** Query to get unique live with given webid. */
    private static final String LIVE_DOC_WEB_ID_QUERY = "select * from Document where ttc:webid = '%s'"
            + " AND ecm:currentLifeCycleState!='deleted' AND ecm:isCheckedInVersion = 0 AND ecm:isProxy = 0";

    /**
     * @param coreSession
     * @param webId
     * @return unique live document given its webid.
     */
    public static DocumentModel getLiveDocumentByWebId(CoreSession coreSession, String webId) {
        DocumentModel live = null;

        String query = String.format(LIVE_DOC_WEB_ID_QUERY, webId);
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
    public static DocumentModelList getDocumentsByWebId(CoreSession coreSession, String parentId, String parentPath, boolean draft, String webId)
            throws NoSuchDocumentException {

        DocumentModelList documents = null;

        if (StringUtils.isNotBlank(webId)) {


            UnrestrictedFecthWebIdRunner fecthWebIdRunner = new UnrestrictedFecthWebIdRunner(coreSession, parentId, parentPath, draft, webId);
            fecthWebIdRunner.runUnrestricted();
            documents = fecthWebIdRunner.getDocuments();

            if (CollectionUtils.isEmpty(documents)) {
                throw new NoSuchDocumentException(webId);
            }

        }

        return documents;

    }

    /**
     * Get doc by webid in unrestricted mode (admin)
     */
    private static class UnrestrictedFecthWebIdRunner extends UnrestrictedSessionRunner {

        String parentId;
        String parentPath;
        boolean draft;
        String webId;
        DocumentModelList documents;

        private static final String PROXY_FILTER = " AND ecm:isProxy = 1 ";
        private static final String LIVE_FILTER = " AND ecm:isProxy = 0 ";

        public UnrestrictedFecthWebIdRunner(CoreSession session, String parentId, String parentPath, boolean draft, String webId) {
            super(session);
            this.parentPath = parentPath;
            this.parentId = parentId;
            this.draft = draft;
            this.webId = webId;
            this.documents = new DocumentModelListImpl();
        }

        @Override
        public void run() throws ClientException {
            /*
             * Parent id given: we try to resolve document with webid
             * according to it.
             * Note: we are necessary in case of many remote publications.
             */
            if (StringUtils.isNotBlank(this.parentId)) {
                DocumentModelList proxies = this.session.query(String.format(WEB_ID_QUERY, this.webId, PROXY_FILTER));

                if (CollectionUtils.isNotEmpty(proxies)) {
                    boolean parentIdFound = false;
                    Iterator<DocumentModel> iterator = proxies.iterator();

                    while (iterator.hasNext() && !parentIdFound) {
                        DocumentModel proxy = iterator.next();

                        DocumentModelList parentList = ToutaticeDocumentHelper.getParentList(proxy.getCoreSession(), proxy, null, true, true);
                        if (CollectionUtils.isNotEmpty(parentList)) {
                            DocumentModel firstParent = parentList.get(0);
                            String parentWebId = (String) firstParent.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);

                            if (StringUtils.isNotBlank(parentWebId) && this.parentId.equals(parentWebId)) {
                                this.documents.add(proxy);
                                parentIdFound = true;
                            }
                        }
                    }
                }
            } else if (StringUtils.isNotBlank(this.parentPath)) {

                DocumentModelList proxies = this.session.query(String.format(WEB_ID_QUERY, this.webId, PROXY_FILTER));

                if (CollectionUtils.isNotEmpty(proxies)) {
                    boolean parentPathFound = false;
                    Iterator<DocumentModel> iterator = proxies.iterator();

                    while (iterator.hasNext() && !parentPathFound) {
                        DocumentModel proxy = iterator.next();

                        DocumentModel parentDocument = this.session.getParentDocument(proxy.getRef());
                        if (this.parentPath.equals(parentDocument.getPathAsString())) {
                            this.documents.add(proxy);
                            parentPathFound = true;
                        }
                    }

                }

            } else if (draft) {
                getLive();
            } else {
                /*
                 * Defaul RG:
                 * published -> proxies (for witch user has read permission)
                 * not published -> live (for witch user has read permission)
                 */
                DocumentModelList proxies = this.session.query(String.format(WEB_ID_QUERY, this.webId, PROXY_FILTER));
                if (CollectionUtils.isEmpty(proxies)) {
                    getLive();
                } else {
                    this.documents.addAll(proxies);
                }

            }

        }

        /**
         * Get live with given webId.
         */
        private void getLive() {
            DocumentModelList lives = this.session.query(String.format(WEB_ID_QUERY, this.webId, LIVE_FILTER));
            if (CollectionUtils.isNotEmpty(lives) && lives.size() == 1) {
                this.documents.add(lives.get(0));
            }
        }


        public DocumentModelList getDocuments() {
            return this.documents;
        }

    }


}
