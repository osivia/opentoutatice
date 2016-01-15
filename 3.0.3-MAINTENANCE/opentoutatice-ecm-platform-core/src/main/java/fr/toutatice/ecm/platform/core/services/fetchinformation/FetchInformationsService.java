package fr.toutatice.ecm.platform.core.services.fetchinformation;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface FetchInformationsService extends Serializable {
    
    /**
     * Fetch all document informations for Document.FetchPublicationInfos operation.
     * 
     * @param coreSession
     * @param currentDocument
     * @return
     * @throws ClientException
     */
	Map<String, Object> fetchAllInfos(CoreSession coreSession,
			DocumentModel currentDocument) throws ClientException;
	
	/**
	 * Fetch all document informations for Document.FetchExtendedInfos operation.
	 * 
	 * @param coreSession
	 * @param currentDocument
	 * @return
	 * @throws ClientException
	 */
	Map<String, Object> fetchAllExtendedInfos(CoreSession coreSession,
            DocumentModel currentDocument) throws ClientException;

}
