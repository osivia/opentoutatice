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
 * dchevrier
 * mberhaut1
 */
package fr.toutatice.ecm.platform.service.fragments.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfiguration;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfigurationAdapter;
import fr.toutatice.ecm.platform.core.local.configuration.WebConfsConfigurationConstants;

/**
 * Bean for getting configuration informations
 */
@Name("config")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ConfigurationBeanHelper {

    /**
	 * 
	 */
	private static final String WCONF_OPTIONS = "wconf:options";

	private static final Log log = LogFactory.getLog(ConfigurationBeanHelper.class);

    private static final String WEB_CONFS_QUERY = "select * from WebConfiguration where ecm:ancestorId = '%s' and wconf:type = '%s' "
            + "AND wconf:enabled=1 AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted' ORDER BY ecm:pos";

    /** nagivation context for nuxeo queries */
    @In(create = true)
    protected transient NavigationContext navigationContext;

    /** To get intenationalization values by key. */
    @In(create = true)
    protected Map<String, String> messages;

    /**
     * Get the current JSF instance of this class
     * 
     * @return the instance
     */
    public static ConfigurationBeanHelper getBean() {
        return (ConfigurationBeanHelper) SeamComponentCallHelper.getSeamComponentByName("config");
    }

    /**
     * List of configurations in nuxeo
     * 
     * @return webvconfigurations describing the allowed types of templates the user can set.
     */
    public DocumentModelList getConfigs(String confType) {

        CoreSession session = navigationContext.getOrCreateDocumentManager();
        DocumentModel doc = navigationContext.getCurrentDocument();
        DocumentModel domain = ToutaticeDocumentHelper.getDomain(session, doc, true);

        return getConfigs(confType, session, domain);

    }

    public DocumentModelList getConfigs(String confType, CoreSession session, DocumentModel domain) {
        List<DocumentModel> configs = null;
        
        if (domain != null) {
            UnrestrictedGetAllWebConfs allConfsGetter = new UnrestrictedGetAllWebConfs(session, domain, confType);
            allConfsGetter.runUnrestricted();
            configs = allConfsGetter.getWebConfs();
        }

        DocumentModelList configurations = null;
        if (configs != null) {
            configurations = new DocumentModelListImpl(configs);
        }

        return configurations;
    }

    protected static List<DocumentModel> mergeGlobalNLocalConfs(List<DocumentModel> globalConfs, DocumentModelList localConfs, String confType) {
        List<DocumentModel> mergedConfs = new ArrayList<DocumentModel>(0);

        mergedConfs = getSelectedConfsByType(globalConfs, confType);

        // Diff to remove overriden confs
        /* FIXME: define method to use Collections.removeAll ? */
        for (DocumentModel localConf : localConfs) {
            String localCode = (String) localConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
            boolean hasCode = false;
            for (Iterator<DocumentModel> it = mergedConfs.iterator(); it.hasNext() && !hasCode;) {
                DocumentModel globalConf = it.next();
                String globalCode = (String) globalConf.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_WEB_CONF_CODE);
                if (localCode.equals(globalCode)) {
                    it.remove();
                    mergedConfs.add(localConf);
                    hasCode = true;
                }
            }
        }
        return mergedConfs;
    }

    /**
     * 
     * @param selectedConfs
     * @param confType
     * @return confs of given type.
     */
    protected static List<DocumentModel> getSelectedConfsByType(List<DocumentModel> selectedConfs, String confType) {
        List<DocumentModel> confsByTypes = new ArrayList<DocumentModel>(0);
        for (DocumentModel selectedConf : selectedConfs) {
            String type = (String) selectedConf.getPropertyValue("wconf:type");
            if (confType.equals(type)) {
                confsByTypes.add(selectedConf);
            }
        }
        return confsByTypes;
    }
    
    /**
     * Unrestricted access to global and local web confs.
     * 
     * @author david chevrier.
     *
     */
    public static class UnrestrictedGetAllWebConfs extends UnrestrictedSessionRunner {
        
        private DocumentModel domain;
        private String confType;

        protected UnrestrictedGetAllWebConfs(CoreSession session, DocumentModel domain, String confType) {
            super(session);
            this.domain = domain;
            this.confType = confType;
        }
        
        private List<DocumentModel> webConfs;


        public List<DocumentModel> getWebConfs() {
            return this.webConfs;
        }
        
        @Override
        public void run() throws ClientException {
            
         // Get overriden or new local confs
            String localQuery = String.format(WEB_CONFS_QUERY, this.domain.getId(), this.confType);
            DocumentModelList localConfs = this.session.query(localQuery);
            if (this.domain.hasFacet(WebConfsConfigurationConstants.WEB_CONFS_CONFIGURATION_FACET)) {

                WebConfsConfiguration webConfsConfiguration = this.domain.getAdapter(WebConfsConfiguration.class);
                if (webConfsConfiguration != null) {

                    // Get global selected confs
                    List<DocumentModel> selectedConfs = webConfsConfiguration.getSelectedConfs(this.domain);
                    if (selectedConfs != null) {
                        this.webConfs = new ArrayList<DocumentModel>(mergeGlobalNLocalConfs(selectedConfs, localConfs, this.confType));
                    } else {
                        // TODO: To test!!
                        this.webConfs = new ArrayList<DocumentModel>(getSelectedConfsByType(localConfs, this.confType));
                    }

                }
            } else {

                WebConfsConfigurationAdapter.UnrestrictedGetGlobalWebConfs globalConfsGetter = new WebConfsConfigurationAdapter.UnrestrictedGetGlobalWebConfs(
                        this.session);
                globalConfsGetter.runUnrestricted();
                DocumentModelList globalConfs = globalConfsGetter.getWebConfs();

                if (globalConfs != null && !globalConfs.isEmpty()) {
                    this.webConfs = new ArrayList<DocumentModel>(mergeGlobalNLocalConfs(globalConfs, localConfs, this.confType));
                } else {
                    // TODO: To test!!
                    this.webConfs = new ArrayList<DocumentModel>(getSelectedConfsByType(localConfs, this.confType));
                }
            }
            
        }
        
    }

    /**
     * 
     * @return a list of pages templates allowed in the cms mode
     */
    public DocumentModelList getPageTemplates() {
        return getConfigs("pagetemplate");
    }

    /**
     * 
     * @return a list of pages themes allowed in the cms mode
     */
    public DocumentModelList getPageThemes() {
        return getConfigs("pagetheme");
    }

    /**
     * 
     * @return a list of fragment types who can be created in cms mode
     */
    public DocumentModelList getFragmentTypes() {
        return getConfigs("fragmenttype");
    }

    /**
     * 
     * @return a list of css window style who can be setted on the CMS windows
     */
    public DocumentModelList getWindowStyles() {
        return getConfigs("windowstyle");
    }

    /**
     * 
     * @return a list of templates who can be setted on the list
     */
    public DocumentModelList getListTemplates() {
        return getConfigs("listtemplate");
    }

    /**
     * 
     * @return a list of zoom templates who can be setted on the zoom fragment
     */
    public DocumentModelList getZoomTemplates() {
        return getConfigs("zoomtemplate");
    }


    /**
     * 
     * @return a list of links templates who can be setted on the links fragment
     */
    public DocumentModelList getLinksTemplates() {
        return getConfigs("linkstemplate");
    }

    /**
     * 
     * @return the avaliable regions layouts
     */
    public DocumentModelList getRegionLayouts() {
        return getConfigs("regionlayout");
    }

    /**
     * List of configurations in nuxeo
     * 
     * @return webconfig of portlet
     */
    public List<Map<String, String>> getFragmentOptionsByCode(DocumentModel doc, String code2) {

        try {
            CoreSession session = navigationContext.getOrCreateDocumentManager();

            String type = doc.getType();
            String confPath = null;

            // compute domain path
            DocumentModel child = null;
            while (!(type.equals("Domain")) && doc != child) {
                child = doc;
                doc = session.getDocument(doc.getParentRef());
                if (doc != null) {
                    type = doc.getType();

                    if (type.equals("Domain")) {
                        confPath = doc.getPath().toString();
                    }
                }

            }

            // select conf objects that are enabled
            String query = "select * from Document " + "where ecm:primaryType = 'WebConfiguration'  " + " AND wconf:type =  'fragmenttype'"
                    + " AND wconf:enabled=1  " + " AND ecm:mixinType != 'HiddenInNavigation'  AND ecm:currentLifeCycleState <> 'deleted'  "
                    + " AND wconf:code2 = '" + code2 + "'";

            // if domain is found, query only conf who is belong to it
            if (confPath != null) {
                query = query.concat(" AND ecm:path STARTSWITH '" + confPath + "' ");
            }

            DocumentModelList configurations = session.query(query);

            if (configurations.get(0) != null) {
                DocumentModel config = configurations.get(0);

                Map<String, Object> properties = config.getProperties("webconfiguration");

                if (properties.containsKey(WCONF_OPTIONS) && properties.get(WCONF_OPTIONS) != null)
                    return (List<Map<String, String>>) properties.get(WCONF_OPTIONS);

            }


        } catch (ClientException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Evaluate a configuration option on website options
     * 
     * @param paramName the param name
     * @return true or false
     */
    public boolean getWebsiteParam(String paramName) {
    	
    	boolean conf = false;
    	
    	DocumentModelList configs = getConfigs("websiteConfig");
    	
    	if(configs.size() > 0) {
    		DocumentModel websiteconfig = configs.get(0);
    		
    		Map<String, Object> properties = websiteconfig.getProperties("webconfiguration");
    		
    		List<Map<String, String>> options = (List<Map<String, String>>) properties.get(WCONF_OPTIONS);
    		for(Map<String, String> option : options) {
    			if(option.get("propertyName").equals(paramName)) {
    				conf = BooleanUtils.toBoolean(option.get("propertyDefaultValue"));
    				break;
    			}
    		}

    	}
   	
    	return conf;
    }
}
