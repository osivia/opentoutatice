package fr.toutatice.ecm.platform.core.services.fetchinformation;

import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface FetchInformationProvider {

    Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException;

}
