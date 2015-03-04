package fr.toutatice.ecm.platform.core.services.fetchinformation;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface FetchInformationsService extends Serializable {

	Map<String, String> fetchAllInfos(CoreSession coreSession,
			DocumentModel currentDocument) throws ClientException;

}
