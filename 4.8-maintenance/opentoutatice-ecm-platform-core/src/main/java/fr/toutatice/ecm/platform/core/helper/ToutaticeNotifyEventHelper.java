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
package fr.toutatice.ecm.platform.core.helper;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.platform.audit.api.Logs;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeNotifyEventHelper {
	private static EventProducer eventProducer;
	private static Logs logsBean;
	
	private ToutaticeNotifyEventHelper() {
		// static class, cannot be instantiated
	}
	
    public static void notifyEvent(CoreSession session, String eventName, DocumentModel document, Map<String, Serializable> properties) throws NuxeoException {
    	NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
        DocumentEventContext ctx = new DocumentEventContext(session, principal, document);

        // initialise les propriétés
        if (null != properties) {
        	for (String property : properties.keySet()) {
        		ctx.setProperty(property, properties.get(property));
        	}
        }
        
        // emet l'évènement
        getEventProducer().fireEvent(ctx.newEvent(eventName));
    }
    
    public static void notifyAuditEvent(CoreSession session, String eventName, DocumentModel document, String comment) throws NuxeoException {
    	notifyAuditEvent(session, null, eventName, document, comment);
    }

    public static void notifyAuditEvent(CoreSession session, String category, String eventName, DocumentModel document, String comment) throws NuxeoException {
    	NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
    	DocumentEventContext ctx = new DocumentEventContext(session, principal, document);
    	notifyAuditEvent(ctx, null, eventName, comment);
    }
    	
    public static void notifyAuditEvent(EventContext ctx, String category, String eventName, String comment) throws NuxeoException {
    	ctx.setProperty("category", category);
    	ctx.setProperty("comment", comment);
    	Logs auditProducer = getAuditEventProducer();
    	Event entry = new EventImpl(eventName, ctx);
    	auditProducer.logEvent(entry);
    }

    private static EventProducer getEventProducer() throws NuxeoException {
    	try {
    		if (eventProducer == null) {
    			eventProducer = Framework.getService(EventProducer.class);
    		}
    	} catch (Exception e) {
    		throw new NuxeoException(e);
    	}
    	return eventProducer;
    }

    private static Logs getAuditEventProducer() throws NuxeoException {
    	try {
    		if (logsBean == null) {
    			logsBean = Framework.getService(Logs.class);
    		}
    	} catch (Exception e) {
    		throw new NuxeoException(e);
    	}
    	return logsBean;
    }

}
