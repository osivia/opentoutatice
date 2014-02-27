package fr.toutatice.ecm.platform.web.publication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.publisher.api.PublicationNode;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;
import org.nuxeo.ecm.platform.publisher.impl.core.SimpleCorePublishedDocument;
import org.nuxeo.ecm.platform.publisher.task.CoreProxyWithWorkflowFactory;

import fr.toutatice.ecm.platform.core.helper.ToutaticeCommentsHelper;

public class ToutaticeCoreProxyWithWorkflowFactory extends CoreProxyWithWorkflowFactory {
    
    @Override
    public PublishedDocument publishDocument(DocumentModel doc,
            PublicationNode targetNode, Map<String, String> params)
            throws ClientException {
        PublishedDocument newPulishedDoc = null;
        if(doc.isProxy()){
            Map<DocumentModel, List<DocumentModel>> proxyComments = new HashMap<DocumentModel, List<DocumentModel>>();
            proxyComments.putAll(ToutaticeCommentsHelper.getProxyComments(doc));
            newPulishedDoc = super.publishDocument(doc, targetNode, params);
            DocumentModel newProxy = ((SimpleCorePublishedDocument) newPulishedDoc).getProxy();
            ToutaticeCommentsHelper.setComments(super.coreSession, newProxy, proxyComments);
            super.coreSession.saveDocument(newProxy);
        }else{
            newPulishedDoc = super.publishDocument(doc, targetNode, params); 
        }
        return newPulishedDoc;
    }
    
}
