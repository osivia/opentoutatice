package fr.toutatice.ecm.platform.service.inheritance;

import org.nuxeo.ecm.core.event.Event;

public interface ToutaticeInheritanceService {

	public void runSync(Event event);
	public void runAsync(Event event);
	
}
