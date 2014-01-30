package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;

import org.nuxeo.ecm.core.event.Event;

public class ToutaticeEventFilterHandler<T> implements InvocationHandler {

//	private static final Log log = LogFactory.getLog(ToutaticeEventFilterHandler.class);

	protected final T object;

	public static <T> T newProxy(T object, Class<T> itf) {
		InvocationHandler h = new ToutaticeEventFilterHandler<T>(object);
		return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { itf }, h));
	}

	protected ToutaticeEventFilterHandler(T object) {
		this.object = object;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if ("fireEvent".equals(method.getName())) {
				if (null != args && 0 < args.length) {
					Event evt = (Event) args[0];
					Principal principal = evt.getContext().getPrincipal();
					
					if (null != principal && ToutaticeEventFilterService.instance().isRegistered(principal.getName())) {
						// filterer
						return null;
					}
				}
			}
			
			return method.invoke(object, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

}
