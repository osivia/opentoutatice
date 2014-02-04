package fr.toutatice.ecm.platform.core.services.proxyfactory;

import java.io.Serializable;

public interface ProxyFactoryCfgService<T> extends Serializable {
	
	public Class<?> getServiceHandler(Class<T> clazz) throws ClassNotFoundException;
	
}
