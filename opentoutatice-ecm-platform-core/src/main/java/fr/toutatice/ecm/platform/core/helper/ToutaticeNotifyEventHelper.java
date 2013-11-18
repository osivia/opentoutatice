package fr.toutatice.ecm.platform.core.helper;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.event.Event;
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
	
    public static void notifyEvent(CoreSession session, String eventName, DocumentModel document, Map<String, Serializable> properties) throws ClientException {
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
    
    public static void notifyAuditEvent(CoreSession session, String eventName, DocumentModel document, String comment) throws ClientException {
    	NuxeoPrincipal principal = (NuxeoPrincipal) session.getPrincipal();
    	DocumentEventContext ctx = new DocumentEventContext(session, principal, document);
    	
    	ctx.setComment(comment);
    	Logs auditProducer = getAuditEventProducer();
    	Event entry = new EventImpl(eventName, ctx);
    	auditProducer.logEvent(entry);
    }

    private static EventProducer getEventProducer() throws ClientException {
    	try {
    		if (eventProducer == null) {
    			eventProducer = Framework.getService(EventProducer.class);
    		}
    	} catch (Exception e) {
    		throw new ClientException(e);
    	}
    	return eventProducer;
    }

    private static Logs getAuditEventProducer() throws ClientException {
    	try {
    		if (logsBean == null) {
    			logsBean = Framework.getService(Logs.class);
    		}
    	} catch (Exception e) {
    		throw new ClientException(e);
    	}
    	return logsBean;
    }

}
