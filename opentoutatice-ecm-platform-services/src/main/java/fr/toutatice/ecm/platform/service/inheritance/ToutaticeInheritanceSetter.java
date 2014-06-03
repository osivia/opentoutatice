package fr.toutatice.ecm.platform.service.inheritance;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface ToutaticeInheritanceSetter {

	/**
	 * Core setter processing to update the destination document according to the source document.
	 * The classes that implement this interface have to implement this method to define their own
	 * behavior (what meta-data to update, conditions...)
	 *  
	 * @param session the unrestricted session (silent mode is active)
	 * @param source the source document resulting from the action definition
	 * @param destination the document to update
	 * @return true if the destination document is updated, otherwise false.
	 */
	boolean execute(CoreSession session, DocumentModel source, DocumentModel destination);
	
}
