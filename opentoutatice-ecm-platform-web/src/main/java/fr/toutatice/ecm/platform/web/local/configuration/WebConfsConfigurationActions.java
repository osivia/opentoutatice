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
package fr.toutatice.ecm.platform.web.local.configuration;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfiguration;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfigurationAdapter;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfigurationConstants;

// FIXME: should be in web plugin...
/**
 * @author david chevrier.
 *
 */
@Name("webConfsConfigurationActions")
@Scope(CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
public class WebConfsConfigurationActions implements Serializable {

    private static final long serialVersionUID = -3640808316766352674L;

    @In(create = true, required = true)
    protected transient CoreSession documentManager;

    public List<DocumentModel> getSelectedConfs(DocumentModel document) throws ClientException {
        List<DocumentModel> selectedWebConfs = new ArrayList<DocumentModel>(0);

        WebConfsConfiguration webConfsConfiguration = document.getAdapter(WebConfsConfiguration.class);
        if (webConfsConfiguration != null) {
            selectedWebConfs.addAll(webConfsConfiguration.getSelectedConfs(document));
        }

        return selectedWebConfs;
    }

    public List<DocumentModel> getNotSelectedConfs(DocumentModel document) throws ClientException {
        List<DocumentModel> notSelectedWebConfs = new ArrayList<DocumentModel>(0);

        if (document.hasFacet(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_FACET)) {

            WebConfsConfiguration webConfsConfiguration = document.getAdapter(WebConfsConfiguration.class);
            if (webConfsConfiguration != null) {

                Boolean allDocsDenied = (Boolean) document.getPropertyValue(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_DENIED_ALL_PROPERTY);
                if (BooleanUtils.isNotTrue(allDocsDenied)) {

                    List<String> allowedWebConfs = getAllowedWebConfs(document);
                    notSelectedWebConfs = webConfsConfiguration.getAllGlobalWebConfs(document);

                    for (String webConfCode : allowedWebConfs) {
                        boolean found = false;
                        Iterator<DocumentModel> iterator = notSelectedWebConfs.iterator();
                        while (iterator.hasNext() && !found) {
                            DocumentModel globalWebConf = iterator.next();
                            String confCode = (String) globalWebConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
                            if (StringUtils.isNotBlank(confCode) && webConfCode.equals(confCode)) {
                                iterator.remove();
                                found = true;
                            }
                        }
                    }
                }
            }
        }

        // Collections.sort(selectedTypes, new TypeLabelAlphabeticalOrder(messages));
        return notSelectedWebConfs;
    }

    public List<String> getAllowedWebConfs(DocumentModel doc) throws ClientException {
        List<String> allowedWebConfs = new ArrayList<String>(0);
        WebConfsConfiguration webConfsConfiguration = doc.getAdapter(WebConfsConfiguration.class);
        if (webConfsConfiguration == null) {
            return Collections.emptyList();
        }
        allowedWebConfs.addAll(webConfsConfiguration.getAllowedWebConfs(doc));
        return allowedWebConfs;
    }

    public List<String> getGlobalWebconfsCodes() {
        List<String> gloablWebConfs = new ArrayList<String>(0);

        WebConfsConfigurationAdapter.UnrestrictedGetGlobalWebConfs globalConfsGetter = new WebConfsConfigurationAdapter.UnrestrictedGetGlobalWebConfs(
                documentManager);
        globalConfsGetter.runUnrestricted();
        DocumentModelList webConfs = globalConfsGetter.getWebConfs();
        if (webConfs != null && !webConfs.isEmpty()) {
            for (DocumentModel webConf : webConfs) {
                String code = (String) webConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
                gloablWebConfs.add(code);
            }
        }

        return gloablWebConfs;
    }

}
