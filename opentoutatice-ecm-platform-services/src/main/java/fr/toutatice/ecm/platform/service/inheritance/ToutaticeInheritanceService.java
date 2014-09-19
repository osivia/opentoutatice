package fr.toutatice.ecm.platform.service.inheritance;

import org.nuxeo.ecm.core.event.Event;

public interface ToutaticeInheritanceService {

	public String CTXT_RECURSION_DEPTH_COUNT = "recursionDepthCount";

	public void run(Event event, boolean isSynchronousEvent);
	
}
