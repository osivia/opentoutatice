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
 * lbillon
 */
package fr.toutatice.ecm.platform.automation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;
import fr.toutatice.ecm.platform.service.url.ToutaticeWebIdHelper;

/**
 * Generate or apply a webId on a document. Check if the webId is unique in the domain
 * 
 * @author loic
 * 
 */
@Operation(id = SetWebID.ID, category = Constants.CAT_DOCUMENT, label = "Set webid.",
        description = "Check unicity of webid and apply to the document in current domain..")
public class SetWebID {

    /** Op ID */
    public static final String ID = "Document.SetWebId";

    private static final Log log = LogFactory.getLog(SetWebID.class);

    private static final String NO_RECURSIVE_CHAIN = "notRecursive";

    private static final List<Class<?>> FILTERED_SERVICES_LIST = new ArrayList<Class<?>>() {

        private static final long serialVersionUID = 1L;

        {
            add(EventService.class);
            add(VersioningService.class);
        }
    };
    
    @Context
    protected CoreSession coreSession;

    @In(create = true)
    protected NavigationContext navigationContext;

    @Param(name = "chainSource", required = false)
    protected String chainSource;

    /**
     * Main method
     * 
     * @param doc
     * @return document modified
     * @throws Exception
     */
    @OperationMethod()
    public DocumentModel run(DocumentModel document) throws Exception {
        UnrestrictedSilentSetWebIdRunner runner = new UnrestrictedSilentSetWebIdRunner(this.coreSession, 
                document, this.chainSource);
        runner.silentRun(true, FILTERED_SERVICES_LIST);
        return runner.getDocument();
    }

    public static class UnrestrictedSilentSetWebIdRunner extends ToutaticeSilentProcessRunnerHelper {
        
        private DocumentModel document;
        private String chainSource;
        private DocumentModel parentDoc;

        protected UnrestrictedSilentSetWebIdRunner(CoreSession session, DocumentModel document, 
                String chainSource) {
            super(session);
            this.document = document;
            this.chainSource = chainSource;
        }

        public DocumentModel getDocument() {
            return this.parentDoc != null ? this.parentDoc : this.document;
        }

        @Override
        public void run() throws ClientException {
            String webId = null;
            String extension = null;
            boolean creationMode = false;
            int suffixForUnicity = 1;
            boolean hasToBeUpdated = false;
            
            // if document has not toutatice schema
            if (this.document.isImmutable() || !this.document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
                return;
            }

            // in creation or import, try to generate it.
            if (StringUtils.isBlank(getWebId())) {
                // else new id is generated
                webId = generateWebId(webId, suffixForUnicity);
                // for Files or Pictures : get the extension of the file if exists
                extension = getBlobExtensionIfExists(webId);
                webId = removeBlobExtensionIfExists(webId, extension);
                
                creationMode = true;
            }
            // webid setted in the document, we use it
            else if (StringUtils.isNotBlank(getWebId())) {
                
                webId = getWebId().toString();
                // clean if needed
                // DCH: TEST procedures!
                if(StringUtils.contains(webId, "_")){
                    // Case of technical webId
                    String nonTechPart = StringUtils.substringAfterLast(webId, "_");
                    String techPart = StringUtils.substringBeforeLast(webId, "_");
                    webId = techPart.concat("_").concat(IdUtils.generateId(nonTechPart, "-", true, 24));
                } else {
                    webId = IdUtils.generateId(webId, "-", true, 24);
                }
                
            }
            
            String originalWebid = webId;
            while(isNotUnique(this.session, this.document, webId)){
                webId = generateWebId(originalWebid, suffixForUnicity);
                suffixForUnicity += 1;
                hasToBeUpdated = true;
            }

            if (StringUtils.isNotBlank(webId)) {
                // [others ops like move, restore, ...] don't throw an exception, put a suffix after the id

                // save weburl
                if (hasToBeUpdated || (!hasToBeUpdated && creationMode)) {
                    log.info("Id relocated to " + webId + " for document " + this.document.getPathAsString());
                    this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID, webId);
                    if (extension != null) {
                        this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXTENSION_URL, extension);
                    }
                    this.session.saveDocument(this.document);
                }
            }
            
            if(!StringUtils.equals(chainSource, NO_RECURSIVE_CHAIN)){
                DocumentRef parentRef = this.document.getRef();
                if (this.session.hasChildren(parentRef)) {
                    if (this.parentDoc == null) {
                        this.parentDoc = this.document;
                    }
                    for (DocumentModel child : this.session.getChildren(parentRef)) {
                        this.document = child;
                        run();
                    }
                }
            }

        }

        /**
         * @return
         */
        protected String getWebId() {
            return (String) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
        }
        
        /**
         * @return new webId basedon path.
         */
        protected String generateWebId(String webId, int suffixForUnicity) {
            if(StringUtils.isBlank(webId)){
                String[] arrayPath = this.document.getPathAsString().split("/");
                webId = arrayPath[arrayPath.length - 1];
                // DCH - TO TEST: webId = this.document.getName();
    
                // for docs whose ecm:name may be identical, nuxeo add a .timestamp, remove it
                if (webId.contains(".")) {
                    webId = webId.substring(0, webId.indexOf("."));
                }
            } else {
                webId = webId.concat(String.valueOf(suffixForUnicity));
            }
            return webId;
        }
        
        /**
         * for Files or Pictures : put the extension of the file if exists.
         * 
         * @param webid
         * @return webid with extension.
         */
        protected String getBlobExtensionIfExists(String webid){
            String extension = null;
            
            if ("File".equals(this.document.getType()) || "Picture".equals(this.document.getType())) {
                int lastIndexOf = this.document.getTitle().lastIndexOf(".");
                if (lastIndexOf > -1) {
                    extension = this.document.getTitle().substring(lastIndexOf + 1, this.document.getTitle().length());
                }
            }
            return extension;
        }
        
        /**
         * Remove blob extension from webid (if exists). 
         * 
         * @param webid
         * @param extension
         * @return wenid without blob extension.
         */
        protected String removeBlobExtensionIfExists(String webid, String extension){
            if(StringUtils.isNotBlank(extension)){
                if (webid.endsWith(extension)) {
                    webid = webid.substring(0, webid.length() - extension.length() - 1);
                }
            }
            return webid;
        }
        
        /**
         * Conditional check repository unicity of given webId 
         * 
         * @param ctx
         * @param session
         * @param document
         * @param webId
         * @return true if not unique
         */
//        protected static boolean isNotUnique(OperationContext ctx, CoreSession session, DocumentModel document, 
//                String webId){
//            if(doCheckWebIdUnicty(ctx, document)){
//                return isNotUnique(session, document, webId);
//            }
//            return false;
//        }
        
        /**
         * Checks repository unicity of given webId.
         * 
         * @param webid
         * @return true if not unique
         */
        public static boolean isNotUnique(CoreSession session, DocumentModel document, String webId) {
            String escapedWebId = StringEscapeUtils.escapeJava(webId);
            DocumentModelList query = session.query(String.format(ToutaticeWebIdHelper.WEB_ID_UNICITY_QUERY, escapedWebId, document.getId()));
           return query.size() > 0;
        }
        
    }

}
