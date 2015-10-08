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

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;


/**
 * @author David Chevrier
 * 
 */
public class ToutaticeDocumentResolver {

	private static final Log log = LogFactory
			.getLog(ToutaticeDocumentResolver.class);

	private ToutaticeDocumentResolver() {

	}

	public static DocumentModelList resolveReference(CoreSession session, WedIdRef webIdRef)
			throws DocumentException, ClientException {
		if (webIdRef == null) {
			throw new DocumentException("Invalid reference (null)");
		}
		Object ref = webIdRef.reference();
		if (ref == null) {
			throw new DocumentException("Invalid reference (null)");
		}
		
		return resolveDocumentByWebId(session, webIdRef);
	}

	protected static DocumentModelList resolveDocumentByWebId(CoreSession session, WedIdRef webIdRef) throws ClientException {
	    
		String webId = (String) webIdRef.reference();
		String parentId = null;
		String parentPath = null;
		Map<String, String> parameters = webIdRef.getParameters();
		if(MapUtils.isNotEmpty(parameters)){
		    parentId = parameters.get("parentId");
		    parentPath = parameters.get("parentPath");
		    if(StringUtils.isNotBlank(parentPath)){
		        parentPath = StringUtils.replace(parentPath, "%2F", "/");
		    }
		}
		
		DocumentModelList documents = null;
		try {
		    documents = WebIdResolver.getDocumentsByWebId(session, parentId, parentPath, false, webId);
        } catch (NoSuchDocumentException de) {
           throw new ClientException(de);
        }
		
		return documents;
	}
	
	protected static final void checkPermission(CoreSession session, DocumentModel doc, String permission)
            throws DocumentException, ClientException {
        if (doc != null && !session.hasPermission(doc.getRef(), permission)) {
            throw new DocumentSecurityException("Privilege '" + permission
                    + "' is not granted to '" + session.getPrincipal().getName() + "'");
        }
    }

}
