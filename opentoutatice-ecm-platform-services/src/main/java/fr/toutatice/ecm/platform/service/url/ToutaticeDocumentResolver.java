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
 * 
 */
package fr.toutatice.ecm.platform.service.url;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.security.SecurityConstants;


/**
 * @author David Chevrier
 * 
 */
public class ToutaticeDocumentResolver {

	private static final Log log = LogFactory
			.getLog(ToutaticeDocumentResolver.class);

	private ToutaticeDocumentResolver() {

	}

	public static DocumentModel resolveReference(CoreSession session, WedIdRef webIdRef)
			throws DocumentException, ClientException {
		if (webIdRef == null) {
			throw new DocumentException("Invalid reference (null)");
		}
		Object ref = webIdRef.reference();
		if (ref == null) {
			throw new DocumentException("Invalid reference (null)");
		}
		DocumentModel document = resolveDocumentByWebId(session, webIdRef);
		checkPermission(session, document, SecurityConstants.READ);
		return document;
	}

	protected static DocumentModel resolveDocumentByWebId(CoreSession session, WedIdRef webIdRef) throws ClientException {
		String webId = (String) webIdRef.reference();
		String domainId = webIdRef.getDomainId();
		GetDocsByWebIdUnrestricted runner = new GetDocsByWebIdUnrestricted(session, domainId, webId);
		runner.runUnrestricted();
		DocumentModel doc = runner.getDocument();
		return doc;
	}
	
	private static class GetDocsByWebIdUnrestricted extends UnrestrictedSessionRunner {
		
		private String domainId;
		private String webId;
		private DocumentModel document;
		
		public DocumentModel getDocument(){
			return this.document;
		}
		
		public GetDocsByWebIdUnrestricted(CoreSession session, String domainId, String webId){
			super(session);
			this.domainId = domainId;
			this.webId = webId;
		}

		@Override
		public void run() throws ClientException {
			DocumentModelList docs = null;
			try {
				docs = this.session
						.query("SELECT * FROM Document where ttc:webid = '"
								+ this.webId
								+ "' AND ttc:domainID = '"
								+ this.domainId
								+ "'"
								+ " AND ecm:currentLifeCycleState != 'deleted' AND ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0");
			} catch (ClientException e) {
				log.error("Impossible de déterminer le webID " + e);
			}
			if (docs != null && docs.size() == 1 && docs.get(0) != null) {
				this.document = docs.get(0);
			} else {
				log.error("More than one document with webid: " + webId);
			}
		}
		
	}
	
	protected static final void checkPermission(CoreSession session, DocumentModel doc, String permission)
            throws DocumentException, ClientException {
        if (doc != null && !session.hasPermission(doc.getRef(), permission)) {
            throw new DocumentSecurityException("Privilege '" + permission
                    + "' is not granted to '" + session.getPrincipal().getName() + "'");
        }
    }

}
