package fr.toutatice.ecm.platform.service.quota;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * Dummy quota service (if addon is not setted).
 * 
 * @author Loïc Billon
 */
public class DummyQuotaServiceImpl implements QuotaService {

	@Override
	public Long getFreeSpace(CoreSession session, DocumentModel doc) {

		// Do nothing		
		return null;
	}

}
