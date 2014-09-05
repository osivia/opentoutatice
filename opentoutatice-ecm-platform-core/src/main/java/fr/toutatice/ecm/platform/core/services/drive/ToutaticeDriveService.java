package fr.toutatice.ecm.platform.core.services.drive;

import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;


public interface ToutaticeDriveService {

    Map<String, String> fetchSynchronizationInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException;

}
