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
 * mberhaut1
 * dchevrier
 * lbillon
 */
package fr.toutatice.ecm.platform.core.components;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;

public class ToutaticeEventFilterHandler<T> extends ToutaticeAbstractServiceHandler<T> {

    protected Method[] handledMethods;
    protected final static String HANDLED_METHOD_NAME = "fireEvent";
    private boolean methodsError = false;
    private boolean yetInLogs = false;


    @Override
    public T newProxy(T eventService, Class<T> eventServiceKlass) {
        setObject(eventService);
        getHandledMethods(eventServiceKlass);
        return eventServiceKlass.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{eventServiceKlass}, this));
    }

    protected void getHandledMethods(Class<T> eventServiceKlass) {
        try {
            handledMethods = new Method[2];
            Method firstFireEvent = eventServiceKlass.getMethod(HANDLED_METHOD_NAME, Event.class);
            handledMethods[0] = firstFireEvent;
            Method secondFireEvent = eventServiceKlass.getMethod(HANDLED_METHOD_NAME, String.class, EventContext.class);
            handledMethods[1] = secondFireEvent;
        } catch (Exception e) {
            methodsError = true;
        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (HANDLED_METHOD_NAME.equals(method.getName())) {
                if (methodsError && !yetInLogs) {
                    yetInLogs = true;
                    StringBuilder msgBuilder = new StringBuilder().append(object.getClass().getName()).append(" class has no '").append(HANDLED_METHOD_NAME)
                            .append("' method with given signatures");
                    throw new ToutaticeServiceHandlerException(msgBuilder.toString());
                }

                if (handledMethods != null && handledMethods.length > 0) {
                    if (null != args && args.length > 0) {
                        Principal principal = null;
                        if (handledMethods[0] != null && handledMethods[0].equals(method)) {
                            Event evt = (Event) args[0];
                            principal = evt.getContext().getPrincipal();
                        } else if (handledMethods[1] != null && handledMethods[1].equals(method)) {
                            EventContext evtCtx = (EventContext) args[1];
                            principal = evtCtx.getPrincipal();
                        }
                        if (null != principal && ToutaticeServiceProvider.instance().isRegistered(EventService.class, principal.getName())) {
                            // do filter invocation
                            return null;
                        }
                    }
                }
            }
            return method.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
