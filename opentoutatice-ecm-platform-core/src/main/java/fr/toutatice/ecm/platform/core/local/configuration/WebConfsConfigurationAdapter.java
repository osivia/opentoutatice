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
package fr.toutatice.ecm.platform.core.local.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.Filter;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.localconfiguration.AbstractLocalConfiguration;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;


/**
 * @author david chevrier.
 *
 */
public class WebConfsConfigurationAdapter extends AbstractLocalConfiguration<WebConfsConfiguration> implements WebConfsConfiguration {

    private static final Log log = LogFactory.getLog(WebConfsConfigurationAdapter.class);

    protected DocumentRef documentRef;

    protected List<DocumentModel> allConfsDocs;
    protected List<String> allowedConfsDocs;
    protected boolean denyAllConfsDocs;

    public WebConfsConfigurationAdapter(DocumentModel document) {
        documentRef = document.getRef();
        allowedConfsDocs = getWebConfsList(document, WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_ALLOWED_PROPERTY);
        denyAllConfsDocs = getDenyAllWebConfsProperty(document);
    }

    public List<DocumentModel> getAllConfsDocs() {
        return allConfsDocs;
    }


    public void setAllConfsDocs(List<DocumentModel> allConfsDocs) {
        this.allConfsDocs = allConfsDocs;
    }

    @Override
    public List<String> getAllowedConfsDocs() {
        return allowedConfsDocs;
    }

    @Override
    public boolean getDenyAllConfsDocs() {
        return denyAllConfsDocs;
    }

    @Override
    public DocumentRef getDocumentRef() {
        return this.documentRef;
    }

    protected List<String> getWebConfsList(DocumentModel doc, String property) {
        String[] webConfs;
        try {
            webConfs = (String[]) doc.getPropertyValue(property);
        } catch (NuxeoException e) {
            return Collections.emptyList();
        }
        if (webConfs == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(webConfs));
    }

    protected boolean getDenyAllWebConfsProperty(DocumentModel doc) {
        try {
            Boolean value = (Boolean) doc.getPropertyValue(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_DENIED_ALL_PROPERTY);
            return Boolean.TRUE.equals(value);
        } catch (NuxeoException e) {
            return false;
        }
    }

    public List<DocumentModel> getSelectedConfs(DocumentModel document) throws NuxeoException {
        List<DocumentModel> selectedWebConfs = new ArrayList<DocumentModel>(0);

        if (document.hasFacet(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_FACET)) {

            Boolean allDocsDenied = (Boolean) document.getPropertyValue(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_DENIED_ALL_PROPERTY);
            if (BooleanUtils.isNotTrue(allDocsDenied)) {

                List<String> allowedWebConfs = getAllowedWebConfs(document);
                List<DocumentModel> allglobalWebConfs = getAllGlobalWebConfs(document);

                for (String webConfCode : allowedWebConfs) {
                    boolean found = false;
                    Iterator<DocumentModel> iterator = allglobalWebConfs.iterator();
                    while (iterator.hasNext() && !found) {
                        DocumentModel globalWebConf = iterator.next();
                        String confCode = (String) globalWebConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
                        if (StringUtils.isNotBlank(confCode) && webConfCode.equals(confCode)) {
                            selectedWebConfs.add(globalWebConf);
                            found = true;
                        }
                    }
                }
            }
        }

        // Collections.sort(selectedTypes, new TypeLabelAlphabeticalOrder(messages));
        return selectedWebConfs;
    }

    public List<String> getAllowedWebConfs(DocumentModel doc) throws NuxeoException {
        List<String> allowedWebConfs = new ArrayList<String>(getAllowedConfsDocs());
        if (allowedWebConfs.isEmpty()) {
            allowedWebConfs = computeAllowedWebConfs(getAllGlobalWebConfs(doc));
        }
        return allowedWebConfs;
    }

    protected List<String> computeAllowedWebConfs(List<DocumentModel> webConfsDocs) {
        List<String> webConfsCodes = new ArrayList<String>(0);
        for (Iterator<DocumentModel> iterator = webConfsDocs.iterator(); iterator.hasNext();) {
            DocumentModel webConf = iterator.next();
            String webConfCode = (String) webConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
            webConfsCodes.add(webConfCode);
        }
        return webConfsCodes;
    }

    /**
     * 
     * @return the global available web configurations.
     */
    public List<DocumentModel> getAllGlobalWebConfs(DocumentModel domain) {
        List<DocumentModel> globalWebConfs = new ArrayList<DocumentModel>(0);

        List<DocumentModel> allConfsDocs = getAllConfsDocs();
        if (allConfsDocs != null) {
            globalWebConfs.addAll(allConfsDocs);
        } else {
            CoreSession coreSession = CoreInstance.openCoreSession("default");
            try {
                UnrestrictedGetGlobalWebConfs webConfsGetter = new UnrestrictedGetGlobalWebConfs(coreSession);
                webConfsGetter.runUnrestricted();
                DocumentModelList webConfs = webConfsGetter.getWebConfs();

                if (webConfs != null && !webConfs.isEmpty()) {
                    globalWebConfs.addAll(webConfs);
                    setAllConfsDocs(webConfs);
                }
            } finally {
                coreSession.close();
            }
        }

        return globalWebConfs;

    }


    /**
     * Unrestricted access to global web confs.
     * 
     * @author david chevrier.
     *
     */
    public static class UnrestrictedGetGlobalWebConfs extends UnrestrictedSessionRunner {

        private DocumentModelList webConfs;


        public DocumentModelList getWebConfs() {
            return webConfs;
        }

        public UnrestrictedGetGlobalWebConfs(CoreSession session) {
            super(session);
        }

        @Override
        public void run() throws NuxeoException {
            Filter noDomainParent = new Filter() {

                private static final long serialVersionUID = 1L;

                @Override
                public boolean accept(DocumentModel docModel) {
                    DocumentModel domain = ToutaticeDocumentHelper.getDomain(session, docModel, true);
                    return domain == null;
                }

            };

            DocumentModel repo = this.session.getRootDocument();
            StringBuffer query = new StringBuffer().append("select * from ").append(ToutaticeNuxeoStudioConst.CST_DOC_TYPE_WEB_CONFIGURATION)
                    .append(" where ecm:ancestorId = '").append(repo.getId())
                    .append("' and wconf:enabled=1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted'");
            webConfs = this.session.query(query.toString(), noDomainParent);
        }

    }

}
