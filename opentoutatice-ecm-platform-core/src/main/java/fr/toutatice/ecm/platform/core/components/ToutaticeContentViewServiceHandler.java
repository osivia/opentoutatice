/**
 * 
 */
package fr.toutatice.ecm.platform.core.components;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.nuxeo.ecm.platform.contentview.jsf.ContentViewService;


/**
 * @author David Chevrier
 */
public class ToutaticeContentViewServiceHandler<T> extends ToutaticeAbstractServiceHandler<T> {

    private static final String VIRTUAL_PRINCIPAL = "VT_nx_principal";

    @Override
    public T newProxy(T object, Class<T> itf) {
        setObject(object);
        return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{itf}, this));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (ToutaticeServiceProvider.instance().isRegistered(ContentViewService.class, VIRTUAL_PRINCIPAL)) {
                if ("resolvePageProviderProperties".equals(method.getName())) {
                    return new HashMap<String, Serializable>(0);
                }
            }
            return method.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
