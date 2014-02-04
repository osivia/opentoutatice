package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.api.DefaultServiceProvider;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.ServiceProvider;

import fr.toutatice.ecm.platform.core.services.proxyfactory.ProxyFactoryCfgService;

/**
 * Cette classe permet de filtrer les événements qui sont produits/levés par les différents 
 * traitements et ainsi d'implémenter le mode "silencieux" où les données type dublincore
 * d'un document ne doivent pas être modifiées.
 * 
 * Lorsque le framework sera sollicité (session::getLocalService()) pour obtenir une instance 
 * du service EventService, il obtiendra un proxy sur ce dernier (implémenté par la classe 
 * ToutaticeEventFilterHandler. Le filtrage est nomminatif pour un utilisateur connecté.
 * 
 * @author mberhaut1
 */
public class ToutaticeServiceProvider implements ServiceProvider {

	private static final Log log = LogFactory.getLog(ToutaticeServiceProvider.class);
	
	private boolean installed;
	private ServiceProvider nextProvider;
	private static ToutaticeServiceProvider instance = null;
	private static Map<String, List<String>> filteredUsersMap = null;
	
	protected final Map<Class<?>, Entry<?>> registry = new HashMap<Class<?>, Entry<?>>();

	@SuppressWarnings("rawtypes")
	private ProxyFactoryCfgService pfsService;

	// singleton
	private ToutaticeServiceProvider() {
		installed = false;
		filteredUsersMap = Collections.synchronizedMap(new HashMap<String, List<String>>());
	}
	
	public static ToutaticeServiceProvider instance() {
		if (null == instance) {
			instance = new ToutaticeServiceProvider();
		}
		
		return instance;
	}
	
	public void install() {
		if (installed) {
			return;
		}
		
		installed = true;
		nextProvider = DefaultServiceProvider.getProvider();
		DefaultServiceProvider.setProvider(instance);
	}

	public void uninstall() {
		DefaultServiceProvider.setProvider(nextProvider);
		installed = false;
	}
	
	public void register(Class<?> service, String principalId) {
		String serviceName = service.getName();
		
		synchronized (filteredUsersMap) {
			if (!filteredUsersMap.containsKey(serviceName)) {
				filteredUsersMap.put(serviceName, new ArrayList<String>());
			}
			
			List<String> usersList = filteredUsersMap.get(serviceName);
			if (!usersList.contains(principalId)) {
				usersList.add(principalId);
			}
		}
	}

	public void unregister(Class<?> service, String principalId) {
		String serviceName = service.getName();

		synchronized (filteredUsersMap) {
			if (filteredUsersMap.containsKey(serviceName)) {
				List<String> usersList = filteredUsersMap.get(serviceName);
				usersList.remove(principalId);
			}
		}
	}

	public boolean isRegistered(Class<?> service, String principalId) {
		boolean status = false;

		String serviceName = service.getName();
		synchronized (filteredUsersMap) {
			if (filteredUsersMap.containsKey(serviceName)) {
				List<String> usersList = filteredUsersMap.get(serviceName);
				status = usersList.contains(principalId);
			}
		}		
		return status;
	}

	@Override
	public <T> T getService(Class<T> srvClass) {
		if (!registry.containsKey(srvClass)) {
			registry.put(srvClass, new Entry<T>(srvClass));
		}

		return srvClass.cast(registry.get(srvClass).getService());
	}

	@SuppressWarnings("rawtypes")
	private ProxyFactoryCfgService getProxyFactoryService() {
		if (null == this.pfsService) {
			this.pfsService = nextProvider != null ? nextProvider.getService(ProxyFactoryCfgService.class) : Framework.getRuntime().getService(ProxyFactoryCfgService.class);
		}
		return this.pfsService;
	}

	private class Entry<T> {
		final Class<T> srvClass;

		protected Entry(Class<T> srvClass) {
			this.srvClass = srvClass;
		}

		@SuppressWarnings("unchecked")
		public T getService() {
			T srvObject = nextProvider != null ? nextProvider.getService(srvClass) : Framework.getRuntime().getService(srvClass);
			
			try {
				ProxyFactoryCfgService<T> pfs = getProxyFactoryService();
				Class<?> handler = pfs.getServiceHandler(srvClass);
				if (null != handler) {
					Object ho = handler.newInstance();
					Method themethod = handler.getDeclaredMethod("newProxy", Object.class, srvClass.getClass());
					return (T) themethod.invoke(ho, srvObject, srvClass);
				}
			} catch (Exception e) {
				log.error("Failed to instanciate the service proxy '" + this.srvClass.getName() + " (the native Nuxeo service will be used instead)', error: " + e.getMessage());
			}
			
			return srvObject;			
		}

	}

}
