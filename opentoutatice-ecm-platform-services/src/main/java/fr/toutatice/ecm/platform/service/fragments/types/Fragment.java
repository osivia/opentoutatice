package fr.toutatice.ecm.platform.service.fragments.types;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.fragments.FragmentServiceException;


public interface Fragment {

    public String prepareCreation(DocumentModel doc, String uri, String region, String belowUri, String code2) throws FragmentServiceException;

}
