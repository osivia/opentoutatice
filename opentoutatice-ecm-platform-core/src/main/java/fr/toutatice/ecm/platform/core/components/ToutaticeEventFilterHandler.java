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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;

public class ToutaticeEventFilterHandler<T> extends ToutaticeAbstractServiceHandler<T> {

//	private static final Log log = LogFactory.getLog(ToutaticeEventFilterHandler.class);

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
