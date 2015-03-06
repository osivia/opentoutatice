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
package fr.toutatice.ecm.platform.infos;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.automation.helper.WebIdResolver;
import fr.toutatice.ecm.platform.core.services.fetchinformation.FetchInformationsService;


/**
 * @author david chevrier
 *
 */
@Operation(id = FetchExtendedDocumentInfos.ID, category = Constants.CAT_FETCH, label = "Fetch extended document informations",
description = "Fetch peculiar informations about the given document (used by Portal).")
public class FetchExtendedDocumentInfos {
    
    /** Operation id */
    public static final String ID = "Document.FetchExtendedDocInfos";
    
    /** Session */
    @Context
    protected CoreSession session;
    
    // TODO: webId resolution to have only one id parameter!!
    
    /** Id of document: path, id, webId */
    @Param(name = "path", required = false)
    protected DocumentModel document;
    
    /** WebId of document (if exists) */
    @Param(name = "webid", required = false)
    protected String webid;
    
    
    @OperationMethod
    public Blob run() throws Exception {
        
        JSONArray rowDocInfos= new JSONArray();
        JSONObject docInfos = new JSONObject();
        
        if(StringUtils.isNotBlank(webid)){
            document = WebIdResolver.getDocumentByWebId(session, webid);
        }
        
        FetchInformationsService fetchInfosService = Framework.getService(FetchInformationsService.class);
        if (fetchInfosService != null) {
            Map<String, Object> infos = fetchInfosService.fetchAllExtendedInfos(session, document);
            docInfos.accumulateAll(infos);
        }
        
        rowDocInfos.add(docInfos);
        return new StringBlob(rowDocInfos.toString(), "application/json");
    }

}
