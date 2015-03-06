package fr.toutatice.ecm.platform.core.services.fetchinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewService;
import org.nuxeo.ecm.platform.forms.layout.service.WebLayoutManager;
import org.nuxeo.ecm.platform.types.TypeManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

public class FetchInformationsServiceImpl extends DefaultComponent implements FetchInformationsService {

    private static final long serialVersionUID = 3212642773317224973L;

    public static final String FETCH_INFOS_EXT_POINT = "fetch_infos";
    public static final String EXTENDED_FETCH_INFOS_EXT_POINT = "extended_fetch_infos";

    private List<FetchInformationProvider> contribs;
    private List<FetchInformationProvider> extendedContribs;

    @Override
    public void activate(ComponentContext context) throws Exception {
        super.activate(context);
        contribs = new ArrayList<FetchInformationProvider>(0);
        extendedContribs = new ArrayList<FetchInformationProvider>(0);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        FetchInformationsDescriptor descriptor = (FetchInformationsDescriptor) contribution;
        descriptor.initProvider();
        if (FETCH_INFOS_EXT_POINT.equals(extensionPoint)) {
            contribs.add(descriptor.getInstance());
        } else if(EXTENDED_FETCH_INFOS_EXT_POINT.equals(extensionPoint)){
            extendedContribs.add(descriptor.getInstance());
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) throws Exception {
        FetchInformationsDescriptor descriptor = (FetchInformationsDescriptor) contribution;
        if (FETCH_INFOS_EXT_POINT.equals(extensionPoint)) {
            contribs.remove(descriptor.getInstance());
        } else if(EXTENDED_FETCH_INFOS_EXT_POINT.equals(extensionPoint)){
            extendedContribs.remove(descriptor.getInstance());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchAllInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

        Map<String, Object> infos = new HashMap<String, Object>(0);

        for (FetchInformationProvider contrib : contribs) {
            infos.putAll(contrib.fetchInfos(coreSession, currentDocument));
        }

        return infos;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> fetchAllExtendedInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {
        Map<String, Object> infos = new HashMap<String, Object>(0);

        for (FetchInformationProvider contrib : extendedContribs) {
            infos.putAll(contrib.fetchInfos(coreSession, currentDocument));
        }

        return infos;
    }
}
