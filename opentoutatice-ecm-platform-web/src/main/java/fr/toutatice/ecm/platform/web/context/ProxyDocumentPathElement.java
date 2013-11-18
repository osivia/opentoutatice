package fr.toutatice.ecm.platform.web.context;

import javax.faces.context.FacesContext;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.pathelements.PathElement;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

public class ProxyDocumentPathElement implements PathElement {

	/**
	 * Olivier Adam, Rectorat de Rennes, le 26/10/11
	 */
	private static final long serialVersionUID = -3421858171637763580L;
	private final DocumentModel docModel;
    public static final String TYPE = "ProxyPathElement";
    
	public ProxyDocumentPathElement(DocumentModel docModel) {
        this.docModel = docModel;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName() {
        FacesContext context = FacesContext.getCurrentInstance();
        return ComponentUtils.translate(context, "label.toutatice.version.published");
	}

	@Override
	public boolean isLink() {
		return false;
	}
	
    public DocumentModel getDocumentModel() {
        return docModel;
    }

}
