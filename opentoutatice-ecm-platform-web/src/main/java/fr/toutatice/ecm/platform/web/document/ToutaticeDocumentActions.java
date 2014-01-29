package fr.toutatice.ecm.platform.web.document;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

public interface ToutaticeDocumentActions extends DocumentActions {

	 public List<String> getDocumentPathSegments(DocumentModel document, DocumentModel referenceDoc);
	 public boolean hasProxy(DocumentModel document) throws ClientException;
	 public DocumentModel getProxy(DocumentModel document) throws ClientException;
	 public boolean belongToPublishSpace();
	 public boolean belongToWorkSpace();
	 
	 public String getDocumentPermalink();
	 
}
