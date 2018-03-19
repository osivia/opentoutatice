package fr.toutatice.ecm.platform.service.inheritance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

public class ToutaticeInheritanceSynchListener implements EventListener {
	
	private static final Log log = LogFactory.getLog(ToutaticeInheritanceSynchListener.class);
	
	private ToutaticeInheritanceService service;

	@Override
	public void handleEvent(Event event) throws NuxeoException {
		if (event.getContext() instanceof DocumentEventContext) {
			try {
				getInheritanceService().run(event, true);
			} catch (Exception e) {
				log.error("Failed to request the data inheriatnce service, error: " + e.getMessage());
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
