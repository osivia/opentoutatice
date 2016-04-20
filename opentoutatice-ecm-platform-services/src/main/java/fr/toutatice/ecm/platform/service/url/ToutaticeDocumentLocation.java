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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.Filter;
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
	private Map<String, String> parameters;
	
	public String getServerName() {
		return serverName;
	}
	
	public WebIdRef getWebIdRef() {
		return webIdRef;
	}
	
	public Map<String, String> getParameters(){
	    return this.parameters;
	}

	public ToutaticeDocumentLocation(final String serverName,
			final WebIdRef docRef) {
		super(serverName, docRef);
		this.serverName = serverName;
		this.webIdRef = docRef;
	}
	
	public ToutaticeDocumentLocation(DocumentModel doc){
		super(doc);
		try {
			String webId = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
			String explicitUrl = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXPLICIT_URL);
			String extensionUrl = (String) doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXTENSION_URL);
			this.webIdRef = new WebIdRef(explicitUrl, webId, extensionUrl);
			setLocationParameters(doc);
		} catch (Exception e) {
			log.error("Can not get webId property: " + e.getMessage());
		} 
         
	}
	
	protected void setLocationParameters(DocumentModel doc){
	    this.parameters = new HashMap<String, String>(0);
	    
	    // Case of remote proxy in publish space
	    if(doc.isProxy() && ToutaticeDocumentHelper.isInPublishSpace(doc.getCoreSession(), doc)){
	        
	        if(ToutaticeDocumentHelper.isRemoteProxy(doc)){
	                
	                // If document has firstparent with webId, we take it
	                // otherwise we take parent path
	            DocumentModelList parentList = ToutaticeDocumentHelper.getParentList(doc.getCoreSession(), doc, null, true, true);
	            if(CollectionUtils.isNotEmpty(parentList)){
	                DocumentModel firstParent = parentList.get(0);
	            
	                String parentWebId = (String) firstParent.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
	                
	                if(StringUtils.isNotEmpty(parentWebId)){
	                    this.parameters.put("parentId", parentWebId);
	                } else {
	                    this.parameters.put("parentPath", firstParent.getPathAsString());
	                }
	            }  
	        }
	    }
	}

}
