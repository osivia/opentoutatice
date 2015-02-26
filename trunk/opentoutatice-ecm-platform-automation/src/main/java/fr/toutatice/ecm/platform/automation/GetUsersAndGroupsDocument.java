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
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.automation;



import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.ACL;


@Operation(id = GetUsersAndGroupsDocument.ID, 
category = Constants.CAT_SERVICES,
label = "Get users and groups document", 
since = "5.4",
description = "List users and groups allowed to read the document.")


public class GetUsersAndGroupsDocument {
	
	    public static final String ID = "Document.GetUsersAndGroupsDocument";
	   
	    @OperationMethod
	    public Blob run(DocumentModel doc) throws Exception 
	    {
	    	JSONArray rows = new JSONArray();
	    	
	    	ACP acp = doc.getACP();
	    	ACL[] aclTab = acp.getACLs();
	    	
	    	for(int i=0;i<aclTab.length;i++){
	    		ACL acl = aclTab[i];
	    		ACE[] aceTab = acl.getACEs();
	    		
	    		for(int j=0;j<aceTab.length;j++){
	    			ACE ace = aceTab[j];
	    			JSONObject obj = new JSONObject();
	    			if(ace.isGranted()){
	    				obj.element("userOrGroup", ace.getUsername());
	    				obj.element("permission", ace.getPermission());
	    				rows.add(obj);
	    			}
	    		}
	    	}
	        
	    	if(rows.size()>0){
	    		return new StringBlob(rows.toString(), "application/json");
	    	}else{
	    		return null;
	    	}
	       
	    }

	 
}
