package fr.toutatice.ecm.platform.service.fragments;

import java.util.Map;
import java.util.Map.Entry;

import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.service.fragments.types.Fragment;


public interface FragmentService {

    public Map.Entry<FragmentDescriptor, Fragment> findByCode(String code) throws FragmentServiceException;

    public Entry<FragmentDescriptor, Fragment> getFragmentCategory(DocumentModel doc, String uri) throws FragmentServiceException;

    public String prepareCreation(DocumentModel doc, Fragment specific, String fragmentCategory, String region, String belowUri, String code2)
            throws FragmentServiceException;

}
