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

		InnerSilentModeUpdateDomainID runner = new InnerSilentModeUpdateDomainID(coreSession, doc);
		runner.silentRun(true);
		doc = runner.getDocument();

		return doc;
	}



    private class InnerSilentModeUpdateDomainID extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;

        public InnerSilentModeUpdateDomainID(CoreSession session, DocumentModel document) {
            super(session);
            this.document = document;
        }

        public DocumentModel getDocument() throws ClientException {
			/* Récupération du document dans la session utilisateur connecté afin
			 * que les opérations chaînées suivantes (exécutées avec la session utilisateur) 
			 * soient en mesure de récupérer les propriétés du document.  
			 */
			return this.session.getDocument(this.document.getRef());			 
        }

        @Override
        public void run() throws ClientException {
            try {
				updateDoc(this.document);
			} catch (Exception e) {
				throw new ClientException(e);
			}
        }

        private void updateDoc(DocumentModel document) throws Exception {
        	//get domainID
        	DocumentModel domain = getDomain(this.document);
        	
        	if(domain!=null){
        		String domainID = (String) domain.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_DOMAIN_ID);
                 		
	            /* Set domainId on created document */
	            if(StringUtils.isBlank(domainID)){
	                domainID = domain.getName();
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
	                        updateDoc(child);
	                    }
	                }
	            }
	         }
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

            DocumentModelList domainList = ToutaticeDocumentHelper.getParentList(this.session, doc, domainFilter, false, false, true);

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

    }

}
