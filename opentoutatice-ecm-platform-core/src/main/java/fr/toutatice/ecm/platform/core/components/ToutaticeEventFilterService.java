package fr.toutatice.ecm.platform.core.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.runtime.api.DefaultServiceProvider;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.api.ServiceProvider;

/**
 * Cette classe permet de filtrer les événements qui sont produits/levés par les différents 
 * traitements et ainsi d'implémenter le mode "silencieux" où les données type dublincore
 * d'un document ne doivent pas être modifiées.
 * 
 * Lorsque le framework sera sollicité (session::getLocalService()) pour obtenir une instance 
 * du service EventService, il obtiendra un proxy sur ce dernier (implémenté par la classe 
 * AcarenEventFilterHandler. Le filtrage est nomminatif pour un utilisateur connecté.
 * 
 * @author mberhaut1
 */
public class ToutaticeEventFilterService implements ServiceProvider {

//	private static final Log log = LogFactory.getLog(AcarenEventFilterService.class);
	
	private boolean installed;
	private ServiceProvider nextProvider;
	private static ToutaticeEventFilterService instance = null;
	private static List<String> filteredUsersList;
	
	protected final Map<Class<?>, Entry<?>> registry = new HashMap<Class<?>, Entry<?>>();

	// singleton
	private ToutaticeEventFilterService() {
		filteredUsersList = Collections.synchronizedList(new ArrayList<String>());
		installed = false;
	}
	
	public static ToutaticeEventFilterService instance() {
		if (null == instance) {
			instance = new ToutaticeEventFilterService();
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
	
	public void register(String user) {
		synchronized (filteredUsersList) {
			if (!filteredUsersList.contains(user)) {
				filteredUsersList.add(user);
			}
		}
	}
	
	public void unregister(String user) {
		synchronized (filteredUsersList) {
			filteredUsersList.remove(user);
		}
	}
	
	public boolean isRegistered(String user) {
		return filteredUsersList.contains(user);
	}

	@Override
	public <T> T getService(Class<T> srvClass) {
		if (!registry.containsKey(srvClass)) {
			registry.put(srvClass,  new Entry<T>(srvClass));
		}

		return srvClass.cast(registry.get(srvClass).getService());
	}

	private class Entry<T> {
		final Class<T> srvClass;

		protected Entry(Class<T> srvClass) {
			this.srvClass = srvClass;
		}

		public T getService() {
			T srvObject = nextProvider != null ? nextProvider.getService(srvClass) : Framework.getRuntime().getService(srvClass);
			if (srvObject instanceof EventService) {
				return newProxy(srvObject, srvClass);
			} else {
				return srvObject;
			}
		}

		protected T newProxy(T object, Class<T> clazz) {
			return ToutaticeEventFilterHandler.newProxy(object, clazz);
		}
	}

}
