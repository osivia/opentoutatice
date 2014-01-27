package fr.toutatice.ecm.platform.web.document;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.webapp.documenttemplates.DocumentTemplatesActionsBean;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

@Name("documentTemplatesActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeDocumentTemplatesActionsBean extends DocumentTemplatesActionsBean {

	private static final long serialVersionUID = 5605205971806068358L;
	
	public String createDocumentFromTemplate(String viewId) throws ClientException {
		if (null == this.changeableDocument) {
			this.changeableDocument = this.navigationContext.getChangeableDocument();
		}
		
		super.createDocumentFromTemplate();
		return viewId;
	}

}
