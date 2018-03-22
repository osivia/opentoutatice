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
 * 
 */
package fr.toutatice.ecm.platform.core.components;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.local.LocalSession;
import org.nuxeo.ecm.core.versioning.VersioningService;

public class ToutaticeVersioningServiceHandler<T> extends ToutaticeAbstractServiceHandler<T> {

    private static final Map<String, Object> filteredMethodsList = new HashMap<String, Object>() {

        private static final long serialVersionUID = 1L;

        {
            // No checkout on save
            put("isPreSaveDoingCheckOut", false);
            put("doPreSave", null);
            // No checkin on save
            put("isPostSaveDoingCheckIn", false);
            put("doPostSave", null);
            // No label setting version on create
            put("doPostCreate", null);
        }
    };

    @Override
    public T newProxy(T object, Class<T> itf) {
        setObject(object);
        return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{itf}, this));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String sessionId = null;

        try {
            if (filteredMethodsList.keySet().contains(method.getName())) {

                if (null != args && 0 < args.length) {
                    for (Object arg : args) {
                        if (arg != null) {
                            // Ing args order:
                            // arg can be a CoreSession: doPreSave, doPostSave
                            if (CoreSession.class.isAssignableFrom(arg.getClass())) {
                                sessionId = ((LocalSession) arg).getSessionId();
                                break;
                            }

                            // No CoreSession in signature: arg can be a Map: isPreSaveDoingCheckOut,
                            // isPostSaveDoingCheckIn, doPostCreate
                            if (Map.class.isAssignableFrom(arg.getClass())) {
                                Map<String, Serializable> options = (Map<String, Serializable>) arg;
                                DocumentModel previousDoc = (DocumentModel) options.get(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);
                                if (previousDoc != null) {
                                    CoreSession session = previousDoc.getCoreSession();
                                    if (session != null) {
                                        sessionId = session.getSessionId();
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (StringUtils.isNotBlank(sessionId) && ToutaticeServiceProvider.instance().isRegistered(VersioningService.class, sessionId)) {
                        // do filter invocation
                        return filteredMethodsList.get(method.getName());
                    }
                }
            }

            return method.invoke(this.object, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

}
