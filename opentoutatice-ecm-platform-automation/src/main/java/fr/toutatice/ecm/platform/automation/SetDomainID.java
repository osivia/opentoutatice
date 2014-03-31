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
package fr.toutatice.ecm.platform.automation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.model.PropertyException;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;


/**
 * @author David Chevrier
 */
@Operation(id = SetDomainID.ID, category = Constants.CAT_DOCUMENT, label = "Set domainId.",
        description = "Update domainID's value of document when created and its children when moved.")
public class SetDomainID {

    public static final String ID = "Document.SetDomainID";

    private static final Log log = LogFactory.getLog(SetDomainID.class);

    @Context
    protected CoreSession coreSession;

    @OperationMethod()
    public DocumentModel run(DocumentModel doc) throws Exception {
        if (!doc.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
            return doc;
        }

        DocumentModel domain = getDomain(doc);
        if (domain != null) {
            String domainID = (String) domain.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_DOMAIN_ID);
            String defaultValue = domain.getName();
            InnerSilentModeUpdateDomainID runner = new InnerSilentModeUpdateDomainID(coreSession, doc, domainID, defaultValue);
            runner.silentRun(true);
            doc = runner.getDocument();
        } // else, ignore

        return doc;
    }

    private DocumentModel getDomain(DocumentModel doc) throws Exception {
        DocumentModel domain = null;

        Filter domainFilter = new Filter() {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean accept(DocumentModel document) {
                boolean res = false;
                if (isADomain(document)) {
                    res = true;
                }
                return res;
            }

            private boolean isADomain(DocumentModel document) {
                return ToutaticeNuxeoStudioConst.CST_DOC_TYPE_DOMAIN.equals(document.getType());
            }
        };

        DocumentModelList domainList = ToutaticeDocumentHelper.getParentList(coreSession, doc, domainFilter, true, false, true);

        if (domainList.size() == 1) {
            domain = domainList.get(0);
            if (domain == null) {
                log.warn("Document " + doc.getPathAsString() + " has Domain null: domainId can not be set");
            }
        } else {
            log.warn("Document " + doc.getPathAsString() + " has more than one or no Domain: domainId can not be set");
        }

        return domain;
    }

    private class InnerSilentModeUpdateDomainID extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;
        private String domainID;
        private String defaulValue;

        public InnerSilentModeUpdateDomainID(CoreSession session, DocumentModel document, String domainID, String defaulValue) {
            super(session);
            this.document = document;
            this.domainID = domainID;
            this.defaulValue = defaulValue;
        }

        public DocumentModel getDocument() {
            return this.document;
        }

        @Override
        public void run() throws ClientException {
            updateDoc(this.document, this.domainID, this.defaulValue);
        }

        private void updateDoc(DocumentModel document, String domainID, String defaultValue) throws PropertyException, ClientException {
            /* Set domainId on created document */
            if(StringUtils.isBlank(domainID)){
                domainID = defaultValue;
            }
            document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_DOMAIN_ID, domainID);
            this.session.saveDocument(document);

            /* Moved document case: propagate to children */
            if (document.isFolder()) {
                StringBuilder query = new StringBuilder();
                query.append("select * from Document where ecm:mixinType != 'HiddenInNavigation' AND ecm:isCheckedInVersion = 0 AND ");
                query.append("ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND ecm:parentId = '");
                query.append(document.getId());
                query.append("'");

                DocumentModelList children = this.session.query(query.toString());

                if (children != null && !children.isEmpty()) {
                    for (DocumentModel child : children) {
                        updateDoc(child, domainID, defaultValue);
                    }
                }
            }

        }

    }

}
