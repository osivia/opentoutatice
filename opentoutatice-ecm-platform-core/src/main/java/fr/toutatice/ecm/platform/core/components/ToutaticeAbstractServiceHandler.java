package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class ToutaticeAbstractServiceHandler<T> implements InvocationHandler {
	protected boolean isActivated;
	protected T object;

	protected ToutaticeAbstractServiceHandler() {
		this.object = null;
		this.isActivated = false;
	}
	
	protected ToutaticeAbstractServiceHandler(T object) {
		this();
		this.object = object;
	}
	
	public void setObject(T object) {
		this.object = object;
	}
	
	public void activate() {
		this.isActivated = true;
	}
	
	public void deactivate() {
		this.isActivated = false;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(object, args);
	}

	public abstract T newProxy(T object, Class<T> itf);
}
