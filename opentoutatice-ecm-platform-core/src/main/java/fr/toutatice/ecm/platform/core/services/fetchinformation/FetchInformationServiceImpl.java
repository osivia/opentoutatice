package fr.toutatice.ecm.platform.core.services.fetchinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class FetchInformationServiceImpl extends DefaultComponent implements FetchInformationsService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3212642773317224973L;
	
	private static final List<FetchInformationProvider> contribs = new ArrayList<FetchInformationProvider>();
	
	@Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {

		FetchInformationDescriptor descriptor = (FetchInformationDescriptor) contribution;
		
		descriptor.initProvider();
		
		contribs.add(descriptor.getInstance());
	}
	
	@Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
		
		FetchInformationDescriptor descriptor = (FetchInformationDescriptor) contribution;
		
		contribs.remove(descriptor.getInstance());
	}
	
	public Map<String, String> fetchAllInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
		
		Map<String, String> infos = new HashMap<String, String>();
		
		for(FetchInformationProvider contrib : contribs) {
			infos.putAll(contrib.fetchInfos(coreSession, currentDocument));
		}
		
		return infos;
	}
}
