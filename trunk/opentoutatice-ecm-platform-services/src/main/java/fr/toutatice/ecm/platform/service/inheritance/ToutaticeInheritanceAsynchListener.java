package fr.toutatice.ecm.platform.service.inheritance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeInheritanceAsynchListener implements PostCommitEventListener {
	
	private static final Log log = LogFactory.getLog(ToutaticeInheritanceAsynchListener.class);
	
	private ToutaticeInheritanceService service;

	@Override
	public void handleEvent(EventBundle events) throws ClientException {
        for (Event event : events) {
        	if (event.getContext() instanceof DocumentEventContext) {
        		try {
        			getInheritanceService().run(event, false);
        		} catch (Exception e) {
        			log.error("Failed to request the data inheritance service, error: " + e.getMessage());
        		}
        	}
		}
	}
	
	private ToutaticeInheritanceService getInheritanceService() throws Exception {
		if (null == this.service) {
			this.service = Framework.getService(ToutaticeInheritanceService.class);
		}
		
		return this.service;
	}

}
