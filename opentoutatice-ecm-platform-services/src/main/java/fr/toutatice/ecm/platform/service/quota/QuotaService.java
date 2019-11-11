package fr.toutatice.ecm.platform.service.quota;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Quota service for Nuxeo UI
 * 
 * @author Loïc Billon
 */
public interface QuotaService {

	/**
	 * Get free space for upload a document
	 * 
	 */
	public Long getFreeSpace(CoreSession session, DocumentModel doc);
}
