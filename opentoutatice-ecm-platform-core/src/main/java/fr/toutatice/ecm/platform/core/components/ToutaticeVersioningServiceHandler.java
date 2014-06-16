/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.core.components;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLDocumentLive;

public class ToutaticeVersioningServiceHandler<T> extends ToutaticeAbstractServiceHandler<T> {

//	private static final Log log = LogFactory.getLog(ToutaticeVersioningServiceHandler.class);

	@Override
	public T newProxy(T object, Class<T> itf) {
		setObject(object);
		return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { itf }, this));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		NuxeoPrincipal principal = null;
		
		try {
			if ("doPreSave".equals(method.getName())) {
				if (null != args && 0 < args.length) {
					for (Object arg : args) {
						if (arg instanceof SQLDocumentLive) {
							SQLDocumentLive document = (SQLDocumentLive) args[0];
							Map<String, Serializable> ctxt = document.getSession().getSessionContext();
							principal = (NuxeoPrincipal) ctxt.get("principal");
							break;
						}
					}
					
					if (null != principal && ToutaticeServiceProvider.instance().isRegistered(principal.getName())) {
						// do filter invocation
						return null;
					}
				}
			}
			
			return method.invoke(this.object, args);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
}
