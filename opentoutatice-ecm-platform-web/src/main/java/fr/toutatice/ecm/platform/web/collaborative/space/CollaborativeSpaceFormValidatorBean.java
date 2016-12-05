/**
 * 
 */
package fr.toutatice.ecm.platform.web.collaborative.space;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.ui.web.api.NavigationContext;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;
import org.nuxeo.ecm.webapp.contentbrowser.DocumentActions;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentMetadataHelper;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author david
 *
 */
@Name("csFormValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class CollaborativeSpaceFormValidatorBean implements Serializable {

    private static final long serialVersionUID = -6037133994828912338L;
    
    @In(create = true, required = true)
    protected transient CoreSession documentManager;
    
    @In(create = true, required = true)
    protected transient NavigationContext navigationContext;

    @In(create = true, required = true)
    protected transient DocumentActions documentActions;
    
    /**
     * Validate title unicity in current Folder.
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateTitle(FacesContext context, UIComponent component, Object value) throws ValidatorException {

        if (((ToutaticeDocumentActionsBean) this.documentActions).belongToWorkSpace()) {
            if (!isTileUnique((String) value)) {
                String msg = ComponentUtils.translate(context, "label.cs.validator.no.unique.title");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
        }

    }

    /**
     * Checks title unicity in current Folder.
     * 
     * @param value
     * @return true if title unicity in current Folder
     */
    public boolean isTileUnique(String title) {
        DocumentModel currentDocument = this.navigationContext.getCurrentDocument();
        
        if (currentDocument != null) {
            String currentUUId = null;
            String parentUUId = null;
            
            DocumentModel changeableDocument = this.navigationContext.getChangeableDocument();
            if (changeableDocument != null) {
                // Creation case
                parentUUId = currentDocument.getId();
            } else {
                // Update case
                DocumentModel parentDocument = this.documentManager.getParentDocument(currentDocument.getRef());
                parentUUId = parentDocument.getId();
                currentUUId = currentDocument.getId();
            }
            
            return ToutaticeDocumentMetadataHelper.isTileUnique(this.documentManager, parentUUId, currentUUId, title);
        }
        
        return false;
    }
}
