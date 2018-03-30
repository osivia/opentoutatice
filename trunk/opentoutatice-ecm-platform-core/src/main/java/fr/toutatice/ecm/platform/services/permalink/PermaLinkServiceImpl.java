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
package fr.toutatice.ecm.platform.services.permalink;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.services.permalink.PermaLinkService;
import fr.toutatice.ecm.platform.services.permalink.Permalink;
import fr.toutatice.ecm.platform.services.permalink.PermalinkDescriptor;

public class PermaLinkServiceImpl extends DefaultComponent implements
		PermaLinkService {

	private static final long serialVersionUID = 1L;
	
	public static final ComponentName ID = new ComponentName("fr.toutatice.ecm.platform.service.permalink.PublicationService");

	public static final String PERMALINKS_EXTENSION_POINTS = "permalinks";

	private static final Log log = LogFactory.getLog(PermaLinkServiceImpl.class);

	protected final Map<String, PermalinkDescriptor> descriptors;
	protected final Map<String, Permalink> permalinkImpls;
	private String defaultPermalinkName;

	public PermaLinkServiceImpl() {
		this.descriptors = new HashMap<String, PermalinkDescriptor>();
		this.permalinkImpls = new HashMap<String, Permalink>();
	}

	
	@Override
	public String getPermalink(DocumentModel doc) {		
		return this.getPermalink(doc, getDefaultPermalinkName());
	}
	
	/**
	 * Return the portal host.
	 */
	@Override
	public String getPortalHost(){
	    	    
	    return getPortalHost(getDefaultPermalinkName());
	    
	}

	@Override
	public void deactivate(ComponentContext context) throws Exception {
		descriptors.clear();
		permalinkImpls.clear();
	}

	@Override
	public void registerContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (PERMALINKS_EXTENSION_POINTS.equals(extensionPoint)) {
			PermalinkDescriptor desc = (PermalinkDescriptor) contribution;
			String permalinkName = desc.getName();
			descriptors.put(permalinkName, desc);
			if (desc.getDefaultPermalink())
				defaultPermalinkName = permalinkName;
			permalinkImpls.remove(permalinkName);
			log.debug((new StringBuilder()).append(" Added descriptor ")
					.append(permalinkName).toString());
		}
		log.debug("<-- PublicationServiceImpl.registerContribution");
	}

	@Override
	public void unregisterContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (PERMALINKS_EXTENSION_POINTS.equals(extensionPoint)) {
			PermalinkDescriptor desc = (PermalinkDescriptor) contribution;
			String permalinkName = desc.getName();
			descriptors.remove(permalinkName);
			permalinkImpls.remove(permalinkName);
			log.debug((new StringBuilder()).append(" remove descriptor ")
					.append(permalinkName).toString());
		}
		log.debug("<-- PublicationServiceImpl.unregisterContribution");
	}


	@Override
	public String getPermalink(DocumentModel doc, String codec) {
		String res = null;
		String permalinkName = codec;
		Permalink permalinkImpl = (Permalink) permalinkImpls.get(permalinkName);
		
		PermalinkDescriptor desc = (PermalinkDescriptor) descriptors.get(permalinkName);
		
		if (desc == null){
		    log.warn("No permaLink contribution");
		    return "";
		}
	
		if (permalinkImpl == null) {
			String classPathImpl = desc.getClasspath();
			if (classPathImpl == null){
				throw new IllegalArgumentException(String.format("Unknown classpath for '%s'", permalinkName));
			}	
			try {
				permalinkImpl = (Permalink) Permalink.class.getClassLoader().loadClass(classPathImpl).newInstance();			
				permalinkImpls.put(permalinkName, permalinkImpl);
				
			} catch (Exception e) {
				String msg = String.format("Caught error when instantiating permalink '%s' with class '%s' ",
								permalinkName, classPathImpl);
				log.error(e.getMessage());
				log.error(msg);			
			}
					
		}		
		if(permalinkImpl == null){
			res =  "";
		}else{
			res = permalinkImpl.getPermalink(doc, desc.getHostServer(), desc.getParameters());
		}
		return res;
	
	}


	@Override
	public String getPortalHost(String codec) {
		  
		    PermalinkDescriptor desc = descriptors.get(codec);
		    
		    if (desc == null){
	            log.warn("No permaLink contribution");
	            return StringUtils.EMPTY;
	        }
		    
		    return desc.getHostServer();
	
	}
	
	public String getDefaultPermalinkName() {

		if (defaultPermalinkName != null) {
			for (String name : descriptors.keySet()) {
				if (descriptors.get(name).getDefaultPermalink()) {
					defaultPermalinkName = name;
					break;
				}
			}
		}

		return defaultPermalinkName;
	}

}
