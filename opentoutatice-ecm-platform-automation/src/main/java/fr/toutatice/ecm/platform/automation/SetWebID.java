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

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

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

    private static final String SEARCH_QUERY = "SELECT * FROM Document WHERE %s";

    private static final String CREATE_OR_COPY = "createOrCopyOp";
    private static final String MOVE_OR_RESTORE = "movedOrRestoredOp";
    private static final String MODIFY = "beforeModificationOp";

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
     * @return docment modified
     * @throws Exception
     */
    @OperationMethod()
    public DocumentModel run(DocumentModel doc) throws Exception {
        String webid = null;
        boolean hasToBeUpdated = false;

        // if document has not toutatice schema
        if (!doc.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE)) {
            return doc;
        }


        // if space does not supports webid or if we can not verify it.
        if (!isSpaceSupportsWebId(doc)) {

            Object currentWebId = doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID);
            if (currentWebId != null) {
                if (StringUtils.isNotEmpty(currentWebId.toString())) {

                    // blank the value of the webid document
                    doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID, StringUtils.EMPTY);
                    this.coreSession.saveDocument(doc);
                    return doc;
                }
            }
        }


        // [Creation mode] get the segment path and put it in the webid field

        if (CREATE_OR_COPY.equals(chainSource)) {

            String[] arrayPath = doc.getPathAsString().split("/");
            webid = arrayPath[arrayPath.length - 1];
            
            // for Files or Pictures : put the extension of the file if exists
            if ("File".equals(doc.getType()) || "Picture".equals(doc.getType())) {
                int lastIndexOf = doc.getTitle().lastIndexOf(".");
                if (lastIndexOf > -1) {
                    String extension = doc.getTitle().substring(lastIndexOf, doc.getTitle().length());
                    webid = webid.concat(extension);
                }
                
            }
            
            hasToBeUpdated = true;
        }
        // [other modes] get the current webid
        else {
            if (doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID) != null) {
                webid = doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID).toString();

                // clean if needed
                webid = IdUtils.generateId(webid, "-", true, 24);
            }
        }


        Object domainID = doc.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_DOMAIN_ID);

        // if webid is defined
        if ((webid != null && webid.length() > 0) && domainID != null) {

            if (StringUtils.isNotEmpty(domainID.toString())) {
                // [modification mode] throw an exception : the user has set a wrong id
                if (MODIFY.equals(chainSource)) {

                    String searchDuplicatedWebUrl = "ttc:domainID = '" + domainID.toString() + "' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'"
                            + " AND ttc:webid = '" + webid + "' AND ecm:path <>'" + doc.getPathAsString() + "'";

                    String queryStr = String.format(SEARCH_QUERY, searchDuplicatedWebUrl);

                    DocumentModelList query = coreSession.query(queryStr);

                    if (query.size() > 0) {
                        throw new OperationException("L'identifiant webId est déjà attribué à un autre contenu dans ce domaine.");
                    }
                } else {

                    // [others ops like move, restore, ...] don't throw an exception, put a suffix after the id
                    boolean unicity = true;
                    Integer suffix = null;
                    String webidconcat = webid;
                    do {

                        String searchDuplicatedWebUrl = "ttc:domainID = '" + domainID.toString()
                                + "' AND ecm:isProxy = 0 AND ecm:currentLifeCycleState!='deleted'" + " AND ttc:webid = '" + webidconcat + "' AND ecm:path <> '"
                                + doc.getPathAsString() + "'";

                        String queryStr = String.format(SEARCH_QUERY, searchDuplicatedWebUrl);

                        DocumentModelList query = coreSession.query(queryStr);

                        if (query.size() > 0) {
                            unicity = false;
                            if (suffix == null)
                                suffix = 1;
                            else
                                suffix = suffix + 1;
                            webidconcat = webid.concat(".").concat(suffix.toString());
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
                        doc.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TOUTATICE_WEBID, webid);
                        this.coreSession.saveDocument(doc);
                    }

                }
            }
        }

        return doc;
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
        DocumentModelList spaces = ToutaticeDocumentHelper.getParentSpaceList(coreSession, doc, false, true, true);
        if (spaces.size() > 0) {

            DocumentModel space = spaces.get(0);
            Property hasWebIdEnabled = space.getProperty("ttcs:hasWebIdEnabled");

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
}
