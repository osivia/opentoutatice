/**
 * 
 */
package org.osivia.opentoutatice.web.bean;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;

/**
 * @author david
 *
 */
@Name("documentActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE + 100)
public class WebModeDocumentActionsBean extends ToutaticeDocumentActionsBean {

	private static final long serialVersionUID = 5410768946883126601L;
	
	/**
	 * Default behavior: all Folderish ara shown in menu.
	 * @throws ClientException
	 */
	@Override
	@Observer(value = {EventNames.NEW_DOCUMENT_CREATED})
    public void initShowInMenu() throws ClientException {
		super.initShowInMenu();
		
		DocumentModel newDocument = navigationContext.getChangeableDocument();
		boolean folderish = newDocument.hasFacet("Folderish");
		
		newDocument.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_SIM, folderish);
	}

}
