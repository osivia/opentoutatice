package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class ToutaticeAbstractServiceHandler<T> implements InvocationHandler {
	protected T object;

	protected ToutaticeAbstractServiceHandler() {
		this.object = null;
	}
	
	protected ToutaticeAbstractServiceHandler(T object) {
		this();
		this.object = object;
	}
	
	protected void setObject(T object) {
		this.object = object;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(object, args);
	}
	
	public abstract T newProxy(T object, Class<T> itf);
}
