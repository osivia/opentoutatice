package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;

public class ToutaticeEventFilterHandler<T> extends ToutaticeAbstractServiceHandler<T> {

//	private static final Log log = LogFactory.getLog(ToutaticeEventFilterHandler.class);

//	public ToutaticeEventFilterHandler() {
//		super();
//	}
//	
//	public ToutaticeEventFilterHandler(T object) {
//		super(object);
//	}
	
	@Override
	public T newProxy(T object, Class<T> itf) {
		setObject(object);
		return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { itf }, this));
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if ("fireEvent".equals(method.getName())) {
				if (null != args && 0 < args.length) {
					Event evt = (Event) args[0];
					Principal principal = evt.getContext().getPrincipal();
					
					if (null != principal && ToutaticeServiceProvider.instance().isRegistered(EventService.class, principal.getName())) {
						// do filter invocation
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
