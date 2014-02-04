package fr.toutatice.ecm.platform.core.services.proxyfactory;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.ComponentName;
import org.nuxeo.runtime.model.DefaultComponent;

import fr.toutatice.ecm.platform.core.components.ToutaticeServiceProvider;

public class ProxyFactoryCfgServiceImpl<T> extends DefaultComponent implements ProxyFactoryCfgService<T> {

	private static final long serialVersionUID = 1062195510415601168L;

	public static final ComponentName ID = new ComponentName("fr.toutatice.ecm.platform.service.proxyfactory.ProxyFactoryCfgService");

	private static final Log log = LogFactory.getLog(ProxyFactoryCfgServiceImpl.class);
	public static final String EXTENSION_POINT = "handlers";

	protected final Map<String, ProxyFactoryCfgDescriptor> descriptors;

	public ProxyFactoryCfgServiceImpl() {
		this.descriptors = new HashMap<String, ProxyFactoryCfgDescriptor>();
	}

	@Override
	public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (EXTENSION_POINT.equals(extensionPoint)) {
			ProxyFactoryCfgDescriptor desc = (ProxyFactoryCfgDescriptor) contribution;
			descriptors.put(desc.getServiceClass(), desc);
			log.info((new StringBuilder()).append(" Added descriptor ").append(desc.getServiceClass()).toString());
		}
	}

	@Override
	public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor)
			throws Exception {
		if (EXTENSION_POINT.equals(extensionPoint)) {
			ProxyFactoryCfgDescriptor desc = (ProxyFactoryCfgDescriptor) contribution;
			descriptors.remove(desc.getServiceClass());
			log.info((new StringBuilder()).append(" Removed descriptor ").append(desc.getServiceClass()).toString());
		}
	}
	
	@Override
	public void activate(ComponentContext context) throws Exception {
		super.activate(context);
		
		// Install the service provider
		ToutaticeServiceProvider.instance().install();
	}
	
	@Override
	public void deactivate(ComponentContext context) throws Exception {
		super.deactivate(context);
		descriptors.clear();
		
		// Uninstall the service provider
		ToutaticeServiceProvider.instance().uninstall();
	}
	
	@Override
	public Class<?> getServiceHandler(Class<T> clazz) throws ClassNotFoundException {
		Class<?> t = null;
		String handlerClassName = clazz.getName();
		
		// look among contributions whether a service proxy is configured
		if (this.descriptors.containsKey(clazz.getName())) {
			ProxyFactoryCfgDescriptor handlerDescriptor = this.descriptors.get(clazz.getName());
			handlerClassName = handlerDescriptor.getHandlerClass();
			t = Class.forName(handlerClassName);
		}
		
		return t;
	}

}
