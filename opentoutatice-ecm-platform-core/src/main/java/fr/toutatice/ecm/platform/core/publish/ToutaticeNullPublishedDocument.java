package fr.toutatice.ecm.platform.core.publish;

import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.publisher.api.PublishedDocument;

public class ToutaticeNullPublishedDocument implements PublishedDocument {

	private static final long serialVersionUID = 1L;

	@Override
	public DocumentRef getSourceDocumentRef() {
		return null;
	}

	@Override
	public String getSourceRepositoryName() {
		return null;
	}

	@Override
	public String getSourceServer() {
		return null;
	}

	@Override
	public String getSourceVersionLabel() {
		return null;
	}

	@Override
	public String getPath() {
		return "/NULL_PUBLISHED_DOCUMENT_PATH";
	}

	@Override
	public String getParentPath() {
		return null;
	}

	@Override
	public boolean isPending() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.LOCAL;
	}

}
