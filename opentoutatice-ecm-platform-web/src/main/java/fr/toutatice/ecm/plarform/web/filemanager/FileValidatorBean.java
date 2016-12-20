/**
 * 
 */
package fr.toutatice.ecm.plarform.web.filemanager;

import java.io.Serializable;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.ui.web.component.file.InputFileInfo;
import org.nuxeo.ecm.platform.ui.web.util.ComponentUtils;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;


/**
 * @author david
 *
 */
@Name("ottcFileValidator")
@Scope(ScopeType.SESSION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class FileValidatorBean implements Serializable {
    
    private static final long serialVersionUID = 6142695676085523525L;
    
    /** Media primary mime-types. */
    public static final String[] MEDIA_PRIMARY_MIME_TYPES = {"image", "audio", "video"};
    
    /**
     * Checks if File has no media type file content.
     * 
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validateMimeType(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(value != null){
            InputFileInfo file = (InputFileInfo) value;
            try {
                
                MimeType mimeType = new MimeType((String) file.getMimeType());
                if(ArrayUtils.contains(MEDIA_PRIMARY_MIME_TYPES, mimeType.getPrimaryType())){
                    String msg = ComponentUtils.translate(context, "ottc.validator.file.bad.mime.type");
                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                    throw new ValidatorException(message);
                }
                
            } catch (MimeTypeParseException e) {
                String msg = ComponentUtils.translate(context, "ottc.validator.file.unknown.mime.type");
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
                throw new ValidatorException(message);
            }
            
        }
    }

}
