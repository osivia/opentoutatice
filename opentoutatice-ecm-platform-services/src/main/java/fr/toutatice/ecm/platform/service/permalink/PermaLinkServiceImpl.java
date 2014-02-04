package fr.toutatice.ecm.platform.service.permalink;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

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
		String res = null;
		String permalinkName = getDefaultPermalinkName();
		Permalink permalinkImpl = (Permalink) permalinkImpls.get(permalinkName);
		
		PermalinkDescriptor desc = (PermalinkDescriptor) descriptors
				.get(permalinkName);

		if (desc == null)
			throw new IllegalArgumentException(String.format(
					"Unknown permalink implementation '%s'", permalinkName));		
	
		if (permalinkImpl == null) {
			String classPathImpl = desc.getClasspath();
			if (classPathImpl == null)
				throw new IllegalArgumentException(String.format(
						"Unknown classpath for '%s'", permalinkName));
			try {
				permalinkImpl = (Permalink) Permalink.class
						.getClassLoader().loadClass(classPathImpl)
						.newInstance();
			} catch (Exception e) {
				String msg = String
						.format("Caught error when instantiating permalink '%s' with class '%s' ",
								permalinkName, classPathImpl);
				throw new IllegalArgumentException(msg, e);
			}
			permalinkImpls.put(permalinkName, permalinkImpl);
						
		}		
		
		res = permalinkImpl.getPermalink(doc, desc.getHostServer(), desc.getParameters());
		return res;
	}

	public String getDefaultPermalinkName() {

		if (defaultPermalinkName != null) {
			for (String name : descriptors.keySet()) {
				if (descriptors.get(name).getEnabled()) {
					defaultPermalinkName = name;
					break;
				}
			}
		}

		return defaultPermalinkName;
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
		log.info("--> PublicationServiceImpl.registerContribution");
		if (PERMALINKS_EXTENSION_POINTS.equals(extensionPoint)) {
			PermalinkDescriptor desc = (PermalinkDescriptor) contribution;
			String permalinkName = desc.getName();
			descriptors.put(permalinkName, desc);
			if (desc.getDefaultPermalink())
				defaultPermalinkName = permalinkName;
			permalinkImpls.remove(permalinkName);
			log.info((new StringBuilder()).append(" Added descriptor ")
					.append(permalinkName).toString());
		}
		log.debug("<-- PublicationServiceImpl.registerContribution");
	}

	@Override
	public void unregisterContribution(Object contribution,
			String extensionPoint, ComponentInstance contributor)
			throws Exception {
		log.info("--> PublicationServiceImpl.unregisterContribution");
		if (PERMALINKS_EXTENSION_POINTS.equals(extensionPoint)) {
			PermalinkDescriptor desc = (PermalinkDescriptor) contribution;
			String permalinkName = desc.getName();
			descriptors.remove(permalinkName);
			permalinkImpls.remove(permalinkName);
			log.info((new StringBuilder()).append(" remove descriptor ")
					.append(permalinkName).toString());
		}
		log.info("<-- PublicationServiceImpl.unregisterContribution");
	}

}
