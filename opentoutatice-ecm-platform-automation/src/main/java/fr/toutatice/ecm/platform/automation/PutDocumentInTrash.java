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

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.trash.TrashService;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author David Chevrier
 */
@Operation(id = PutDocumentInTrash.ID, category = Constants.CAT_DOCUMENT, label = "PutDocumentInTrash", description = "Put a document in trash.")
public class PutDocumentInTrash {
    
    public static final String ID= "Document.PutDocumentInTrash";
    
    @Context
    protected CoreSession session;
    
    @Param(name = "document", required = true)
    protected DocumentModel document;
    
    @OperationMethod
    public Object run() throws Exception {
        List<DocumentModel> docs = new ArrayList<DocumentModel>(1);
        docs.add(document);
        
//    #3411 delete the local proxy if there
        DocumentModel proxy = ToutaticeDocumentHelper.getProxy(session, document, null, true);
        if(proxy!=null){
        	session.removeDocument(proxy.getRef());
        }
        
        
        TrashService trashService = Framework.getService(TrashService.class);
        trashService.trashDocuments(docs);
        
        return document;
        
    }

}
