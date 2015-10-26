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

import java.io.Serializable;
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
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;

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


    private static final String CREATE_OP = "createOp";
    private static final String OTHER_CHAIN = "other";

    private static final String WEB_ID_UNICITY_QUERY = "select * from Document Where ttc:webid = '%s'"
            + " AND ecm:uuid <> '%s' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted' AND ecm:isCheckedInVersion = 0";

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

    @Param(name = "chainSource", required = true)
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
        UnrestrictedSilentSetWebIdRunner runner = new UnrestrictedSilentSetWebIdRunner(coreSession, document);
        runner.silentRun(true, FILTERED_SERVICES_LIST);
        return runner.getDocument();
    }

    /**
     * Get the parent space and look at the property "ttcs:hasWebIdEnabled"
     * 
     * @param doc
     * @return true if webid are enabled
     * @throws PropertyException
     * @throws ClientException
     */
    private boolean isSpaceSupportsWebId(DocumentModel doc) throws PropertyException, ClientException {
        // check if document belong to a space whose supports webid
        boolean spaceSupportsWebId = true;
        DocumentModelList spaces = ToutaticeDocumentHelper.getParentSpaceList(coreSession, doc, true, true, true);
        if (spaces != null && spaces.size() > 0) {

            DocumentModel space = spaces.get(0);
            Property hasWebIdEnabled = space.getProperty(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICESPACE_WEBID_ENABLED);

            if (hasWebIdEnabled != null) {
                if (hasWebIdEnabled.getValue(Boolean.class) == false) {
                    spaceSupportsWebId = false;
                }
            } else {
                spaceSupportsWebId = false; // param in space is set to false
            }
        } else {
            spaceSupportsWebId = false; // space is not found
        }
        return spaceSupportsWebId;
    }

    private class UnrestrictedSilentSetWebIdRunner extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;
        private DocumentModel parentDoc;

        protected UnrestrictedSilentSetWebIdRunner(CoreSession session, DocumentModel document) {
            super(session);
            this.document = document;
        }

        public DocumentModel getDocument() {
            return this.parentDoc != null ? this.parentDoc : this.document;
        }

        @Override
        public void run() throws ClientException {
            String webid = null;
            String extension = null;
            boolean hasToBeUpdated = false;

            // if document has not toutatice schema
            if (this.document.isImmutable() || !this.document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
                return;
            }

            // if space does not supports webid or if we can not verify it.
            if (!isSpaceSupportsWebId(this.document)) {

                Object currentWebId = getWebId();
                if (currentWebId != null) { // in case of import, copy, move or restauration
                    if (StringUtils.isNotEmpty(currentWebId.toString())) {

                        // blank the value of the webid document
                        this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID, StringUtils.EMPTY);
                        this.session.saveDocument(this.document);

                    }
                }

                return;
            }

            // webid setted in the document, we use it
            if (getWebId() != null) {
                webid = getWebId().toString();

                // clean if needed
                webid = IdUtils.generateId(webid, "-", true, 24);
            }
            // else in creation or import, try to generate it.
            else if (CREATE_OP.equals(chainSource) || (OTHER_CHAIN.equals(chainSource) && getWebId() == null)) {
                // else new id is generated
                webid = generateNewWebId();

                // for Files or Pictures : put the extension of the file if exists
                if ("File".equals(this.document.getType()) || "Picture".equals(this.document.getType())) {
                    int lastIndexOf = this.document.getTitle().lastIndexOf(".");
                    if (lastIndexOf > -1) {
                        extension = this.document.getTitle().substring(lastIndexOf + 1, this.document.getTitle().length());

                        if (webid.endsWith(extension)) {
                            webid = webid.substring(0, webid.length() - extension.length() - 1);
                        }
                    }

                }

                hasToBeUpdated = true;

            }

            // if webid is defined
            if (webid != null && webid.length() > 0) {

                // [others ops like move, restore, ...] don't throw an exception, put a suffix after the id
                boolean unicity = true;
                Integer suffix = null;
                String webidconcat = webid;
                do {
                    webidconcat = StringEscapeUtils.escapeJava(webidconcat);
                    DocumentModelList query = coreSession.query(String.format(WEB_ID_UNICITY_QUERY, webidconcat, this.document.getId()));

                    if (query.size() > 0) {
                        unicity = false;
                        if (suffix == null)
                            suffix = 1;
                        else
                            suffix = suffix + 1;
                        webidconcat = webid.concat(suffix.toString());
                    } else {
                        unicity = true;

                        if (!webid.equals(webidconcat)) {
                            webid = webidconcat;
                            hasToBeUpdated = true;
                        }
                    }
                } while (!unicity);

                // save weburl
                if (hasToBeUpdated) {
                    log.warn("Id relocated to " + webid + " for document " + this.document.getPathAsString());
                    this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID, webid);
                    if (extension != null) {
                        this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_EXTENSION_URL, extension);
                    }
                    this.session.saveDocument(this.document);
                }
            }

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

        /**
         * @return
         */
        private Serializable getWebId() {
            return this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
        }

        /**
         * @return new webId basedon path.
         */
        private String generateNewWebId() {
            String webid;
            String[] arrayPath = this.document.getPathAsString().split("/");
            webid = arrayPath[arrayPath.length - 1];

            // for docs whose ecm:name may be identical, nuxeo add a .timestamp, remove it
            if (webid.contains(".")) {
                webid = webid.substring(0, webid.indexOf("."));
            }
            return webid;
        }


    }

}
